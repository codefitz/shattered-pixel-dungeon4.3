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

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.actors.Actor;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.actors.buffs.Burning;
import com.wafitz.pixelspacebase.actors.buffs.LockedDown;
import com.wafitz.pixelspacebase.actors.mobs.Mob;
import com.wafitz.pixelspacebase.effects.particles.ElmoParticle;
import com.wafitz.pixelspacebase.levels.Level;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

class DM3000Armor extends ClassArmor {

    {
        image = ItemSpriteSheet.ARMOR_DM3000;
    }

    @Override
    public void doSpecial() {

        for (Mob mob : Dungeon.level.mobs) {
            if (Level.fieldOfView[mob.pos]) {
                Buff.affect(mob, Burning.class).reignite(mob);
                Buff.prolong(mob, LockedDown.class, 3);
            }
        }

        curUser.HP -= (curUser.HP / 3);

        curUser.spend(Actor.TICK);
        curUser.sprite.operate(curUser.pos);
        curUser.busy();

        curUser.sprite.centerEmitter().start(ElmoParticle.FACTORY, 0.15f, 4);
        Sample.INSTANCE.play(Assets.SND_READ);
    }

}