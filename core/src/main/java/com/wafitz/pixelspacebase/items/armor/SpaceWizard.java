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
package com.wafitz.pixelspacebase.items.armor;

import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.actors.Actor;
import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.actors.buffs.Paralysis;
import com.wafitz.pixelspacebase.effects.CellEmitter;
import com.wafitz.pixelspacebase.effects.Speck;
import com.wafitz.pixelspacebase.mechanics.Ballistica;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.scenes.CellSelector;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

class SpaceWizard extends ClassArmor {

    private static int LEAP_TIME = 1;
    private static int SHOCK_TIME = 3;

    {
        image = ItemSpriteSheet.ARMOR_COMMANDER;
    }

    @Override
    public void doSpecial() {
        GameScene.selectCell(leaper);
    }

    private static CellSelector.Listener leaper = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer target) {
            if (target != null && target != curUser.pos) {

                Ballistica route = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
                int cell = route.collisionPos;

                //can't occupy the same cell as another char, so move back one.
                if (Actor.findChar(cell) != null && cell != curUser.pos)
                    cell = route.path.get(route.dist - 1);


                curUser.HP -= (curUser.HP / 3);

                final int dest = cell;
                curUser.busy();
                curUser.sprite.jump(curUser.pos, cell, new Callback() {
                    @Override
                    public void call() {
                        curUser.move(dest);
                        Dungeon.level.press(dest, curUser);
                        Dungeon.observe();
                        GameScene.updateFog();

                        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                            Char mob = Actor.findChar(curUser.pos + PathFinder.NEIGHBOURS8[i]);
                            if (mob != null && mob != curUser) {
                                Buff.prolong(mob, Paralysis.class, SHOCK_TIME);
                            }
                        }

                        CellEmitter.center(dest).burst(Speck.factory(Speck.DUST), 10);
                        Camera.main.shake(2, 0.5f);

                        curUser.spendAndNext(LEAP_TIME);
                    }
                });
            }
        }

        @Override
        public String prompt() {
            return Messages.get(SpaceWizard.class, "prompt");
        }
    };
}