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
import com.wafitz.pixelspacebase.actors.mobs.Mob;
import com.wafitz.pixelspacebase.items.Item;
import com.wafitz.pixelspacebase.items.weapon.missiles.Shuriken;
import com.wafitz.pixelspacebase.levels.Level;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.wafitz.pixelspacebase.sprites.MissileSprite;
import com.wafitz.pixelspacebase.utils.GLog;
import com.watabou.utils.Callback;

import java.util.HashMap;

class PowerSuit extends ClassArmor {


    {
        image = ItemSpriteSheet.ARMOR_CAPTAIN;
    }

    private HashMap<Callback, Mob> targets = new HashMap<>();

    @Override
    public void doSpecial() {

        Item proto = new Shuriken();

        for (Mob mob : Dungeon.level.mobs) {
            if (Level.fieldOfView[mob.pos]) {

                Callback callback = new Callback() {
                    @Override
                    public void call() {
                        curUser.attack(targets.get(this));
                        targets.remove(this);
                        if (targets.isEmpty()) {
                            curUser.spendAndNext(curUser.attackDelay());
                        }
                    }
                };

                ((MissileSprite) curUser.sprite.parent.recycle(MissileSprite.class)).
                        reset(curUser.pos, mob.pos, proto, callback);

                targets.put(callback, mob);
            }
        }

        if (targets.size() == 0) {
            GLog.w(Messages.get(this, "no_enemies"));
            return;
        }

        curUser.HP -= (curUser.HP / 3);

        curUser.sprite.zap(curUser.pos);
        curUser.busy();
    }

}