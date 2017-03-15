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

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Badges;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.Journal;
import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.items.EquipableItem;
import com.wafitz.pixelspacebase.items.Item;
import com.wafitz.pixelspacebase.items.quest.DarkParts;
import com.wafitz.pixelspacebase.items.quest.Pickaxe;
import com.wafitz.pixelspacebase.items.scripts.ScriptOfUpgrade;
import com.wafitz.pixelspacebase.levels.Room;
import com.wafitz.pixelspacebase.levels.Room.Type;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.wafitz.pixelspacebase.sprites.BlacksmithSprite;
import com.wafitz.pixelspacebase.utils.GLog;
import com.wafitz.pixelspacebase.windows.WndBlacksmith;
import com.wafitz.pixelspacebase.windows.WndQuest;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.Collection;

public class Blacksmith extends NPC {

    {
        spriteClass = BlacksmithSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    @Override
    protected boolean act() {
        throwItem();
        return super.act();
    }

    @Override
    public boolean interact() {

        sprite.turnTo(pos, Dungeon.hero.pos);

        if (!Quest.given) {

            GameScene.show(new WndQuest(this,
                    Quest.alternative ? Messages.get(this, "blood_1") : Messages.get(this, "parts_1")) {

                @Override
                public void onBackPressed() {
                    super.onBackPressed();

                    Quest.given = true;
                    Quest.completed = false;

                    Pickaxe pick = new Pickaxe();
                    if (pick.doPickUp(Dungeon.hero)) {
                        GLog.i(Messages.get(Dungeon.hero, "you_now_have", pick.name()));
                    } else {
                        Dungeon.level.drop(pick, Dungeon.hero.pos).sprite.drop();
                    }
                }
            });

            Journal.add(Journal.Feature.TROLL);

        } else if (!Quest.completed) {
            if (Quest.alternative) {

                Pickaxe pick = Dungeon.hero.belongings.getItem(Pickaxe.class);
                if (pick == null) {
                    tell(Messages.get(this, "lost_pick"));
                } else if (!pick.bloodStained) {
                    tell(Messages.get(this, "blood_2"));
                } else {
                    if (pick.isEquipped(Dungeon.hero)) {
                        pick.doUnequip(Dungeon.hero, false);
                    }
                    pick.detach(Dungeon.hero.belongings.backpack);
                    tell(Messages.get(this, "completed"));

                    Quest.completed = true;
                    Quest.reforged = false;
                }

            } else {

                Pickaxe pick = Dungeon.hero.belongings.getItem(Pickaxe.class);
                DarkParts parts = Dungeon.hero.belongings.getItem(DarkParts.class);
                if (pick == null) {
                    tell(Messages.get(this, "lost_pick"));
                } else if (parts == null || parts.quantity() < 15) {
                    tell(Messages.get(this, "parts_2"));
                } else {
                    if (pick.isEquipped(Dungeon.hero)) {
                        pick.doUnequip(Dungeon.hero, false);
                    }
                    pick.detach(Dungeon.hero.belongings.backpack);
                    parts.detachAll(Dungeon.hero.belongings.backpack);
                    tell(Messages.get(this, "completed"));

                    Quest.completed = true;
                    Quest.reforged = false;
                }

            }
        } else if (!Quest.reforged) {

            GameScene.show(new WndBlacksmith(this, Dungeon.hero));

        } else {

            tell(Messages.get(this, "get_lost"));

        }

        return false;
    }

    private void tell(String text) {
        GameScene.show(new WndQuest(this, text));
    }

    public static String verify(Item item1, Item item2) {

        if (item1 == item2) {
            return Messages.get(Blacksmith.class, "same_item");
        }

        if (item1.getClass() != item2.getClass()) {
            return Messages.get(Blacksmith.class, "diff_type");
        }

        if (!item1.isIdentified() || !item2.isIdentified()) {
            return Messages.get(Blacksmith.class, "un_ided");
        }

        if (item1.malfunctioning || item2.malfunctioning) {
            return Messages.get(Blacksmith.class, "malfunctioning");
        }

        if (item1.level() < 0 || item2.level() < 0) {
            return Messages.get(Blacksmith.class, "degraded");
        }

        if (!item1.isUpgradable() || !item2.isUpgradable()) {
            return Messages.get(Blacksmith.class, "cant_reforge");
        }

        return null;
    }

    public static void upgrade(Item item1, Item item2) {

        Item first, second;
        if (item2.level() > item1.level()) {
            first = item2;
            second = item1;
        } else {
            first = item1;
            second = item2;
        }

        Sample.INSTANCE.play(Assets.SND_EVOKE);
        ScriptOfUpgrade.upgrade(Dungeon.hero);
        Item.evoke(Dungeon.hero);

        if (first.isEquipped(Dungeon.hero)) {
            ((EquipableItem) first).doUnequip(Dungeon.hero, true);
        }
        first.level(first.level() + 1); //prevents on-upgrade effects like enchant/glyph removal
        Dungeon.hero.spendAndNext(2f);
        Badges.validateItemLevelAquired(first);

        if (second.isEquipped(Dungeon.hero)) {
            ((EquipableItem) second).doUnequip(Dungeon.hero, false);
        }
        second.detachAll(Dungeon.hero.belongings.backpack);

        Quest.reforged = true;

        Journal.remove(Journal.Feature.TROLL);
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

    public static class Quest {

        private static boolean spawned;

        private static boolean alternative;
        private static boolean given;
        private static boolean completed;
        private static boolean reforged;

        public static void reset() {
            spawned = false;
            given = false;
            completed = false;
            reforged = false;
        }

        private static final String NODE = "blacksmith";

        private static final String SPAWNED = "spawned";
        private static final String ALTERNATIVE = "alternative";
        private static final String GIVEN = "given";
        private static final String COMPLETED = "completed";
        private static final String REFORGED = "reforged";

        public static void storeInBundle(Bundle bundle) {

            Bundle node = new Bundle();

            node.put(SPAWNED, spawned);

            if (spawned) {
                node.put(ALTERNATIVE, alternative);
                node.put(GIVEN, given);
                node.put(COMPLETED, completed);
                node.put(REFORGED, reforged);
            }

            bundle.put(NODE, node);
        }

        public static void restoreFromBundle(Bundle bundle) {

            Bundle node = bundle.getBundle(NODE);

            if (!node.isNull() && (spawned = node.getBoolean(SPAWNED))) {
                alternative = node.getBoolean(ALTERNATIVE);
                given = node.getBoolean(GIVEN);
                completed = node.getBoolean(COMPLETED);
                reforged = node.getBoolean(REFORGED);
            } else {
                reset();
            }
        }

        public static boolean spawn(Collection<Room> rooms) {
            if (!spawned && Dungeon.depth > 11 && Random.Int(15 - Dungeon.depth) == 0) {

                Room blacksmith;
                for (Room r : rooms) {
                    if (r.type == Type.STANDARD && r.width() > 4 && r.height() > 4) {
                        blacksmith = r;
                        blacksmith.type = Type.BLACKSMITH;

                        spawned = true;
                        alternative = Random.Int(2) == 0;

                        given = false;

                        break;
                    }
                }
            }
            return spawned;
        }
    }
}
