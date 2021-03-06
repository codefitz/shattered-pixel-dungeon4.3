/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.wafitz.pixelspacebase.actors.mobs.npcs;

import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.Journal;
import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.actors.mobs.Golem;
import com.wafitz.pixelspacebase.actors.mobs.Mob;
import com.wafitz.pixelspacebase.actors.mobs.Monk;
import com.wafitz.pixelspacebase.items.Generator;
import com.wafitz.pixelspacebase.items.modules.Module;
import com.wafitz.pixelspacebase.items.quest.DwarfToken;
import com.wafitz.pixelspacebase.levels.CityLevel;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.wafitz.pixelspacebase.sprites.ImpSprite;
import com.wafitz.pixelspacebase.windows.WndArp;
import com.wafitz.pixelspacebase.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Arp extends NPC {

    {
        spriteClass = ImpSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    private boolean seenBefore = false;

    @Override
    protected boolean act() {

        if (!Quest.given && Dungeon.visible[pos]) {
            if (!seenBefore) {
                yell(Messages.get(this, "hey", Dungeon.hero.givenName()));
            }
            seenBefore = true;
        } else {
            seenBefore = false;
        }

        throwItem();

        return super.act();
    }

    @Override
    public int defenseSkill(Char enemy) {
        return 1000;
    }

    @Override
    public void damage(int dmg, Object src) {
    }

    @Override
    public void add(Buff buff) {
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public boolean interact() {

        sprite.turnTo(pos, Dungeon.hero.pos);
        if (Quest.given) {

            DwarfToken tokens = Dungeon.hero.belongings.getItem(DwarfToken.class);
            if (tokens != null && (tokens.quantity() >= 8 || (!Quest.alternative && tokens.quantity() >= 6))) {
                GameScene.show(new WndArp(this, tokens));
            } else {
                tell(Quest.alternative ?
                        Messages.get(this, "monks_2", Dungeon.hero.givenName())
                        : Messages.get(this, "golems_2", Dungeon.hero.givenName()));
            }

        } else {
            tell(Quest.alternative ? Messages.get(this, "monks_1", Dungeon.hero.givenName()) : Messages.get(this, "golems_1", Dungeon.hero.givenName()));
            Quest.given = true;
            Quest.completed = false;

            Journal.add(Journal.Feature.ARP);
        }

        return false;
    }

    private void tell(String text) {
        GameScene.show(
                new WndQuest(this, text));
    }

    public void flee() {

        yell(Messages.get(this, "cya", Dungeon.hero.givenName()));

        destroy();
        sprite.die();
    }

    public static class Quest {

        private static boolean alternative;

        private static boolean spawned;
        private static boolean given;
        private static boolean completed;

        public static Module reward;

        public static void reset() {
            spawned = false;

            reward = null;
        }

        private static final String NODE = "demon";

        private static final String ALTERNATIVE = "alternative";
        private static final String SPAWNED = "spawned";
        private static final String GIVEN = "given";
        private static final String COMPLETED = "completed";
        private static final String REWARD = "reward";

        public static void storeInBundle(Bundle bundle) {

            Bundle node = new Bundle();

            node.put(SPAWNED, spawned);

            if (spawned) {
                node.put(ALTERNATIVE, alternative);

                node.put(GIVEN, given);
                node.put(COMPLETED, completed);
                node.put(REWARD, reward);
            }

            bundle.put(NODE, node);
        }

        public static void restoreFromBundle(Bundle bundle) {

            Bundle node = bundle.getBundle(NODE);

            if (!node.isNull() && (spawned = node.getBoolean(SPAWNED))) {
                alternative = node.getBoolean(ALTERNATIVE);

                given = node.getBoolean(GIVEN);
                completed = node.getBoolean(COMPLETED);
                reward = (Module) node.get(REWARD);
            }
        }

        public static void spawn(CityLevel level) {
            if (!spawned && Dungeon.depth > 16 && Random.Int(20 - Dungeon.depth) == 0) {

                Arp npc = new Arp();
                do {
                    npc.pos = level.randomRespawnCell();
                } while (npc.pos == -1 || level.heaps.get(npc.pos) != null);
                level.mobs.add(npc);

                spawned = true;
                alternative = Random.Int(2) == 0;

                given = false;

                do {
                    reward = (Module) Generator.random(Generator.Category.MODULE);
                } while (reward.malfunctioning);
                reward.upgrade(2);
                reward.malfunctioning = true;
            }
        }

        public static void process(Mob mob) {
            if (spawned && given && !completed) {
                if ((alternative && mob instanceof Monk) ||
                        (!alternative && mob instanceof Golem)) {

                    Dungeon.level.drop(new DwarfToken(), mob.pos).sprite.drop();
                }
            }
        }

        public static void complete() {
            reward = null;
            completed = true;

            Journal.remove(Journal.Feature.ARP);
        }

        public static boolean isCompleted() {
            return completed;
        }
    }
}
