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
package com.wafitz.pixelspacebase.actors.buffs;

import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.hero.Hero;
import com.wafitz.pixelspacebase.actors.mobs.Mob;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.ui.BuffIndicator;
import com.wafitz.pixelspacebase.utils.GLog;

public class Tired extends Buff {

    private static final float STEP = 1f;

    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target) && !target.immunities().contains(Sleep.class)) {

            if (target instanceof Hero)
                if (target.HP == target.HT) {
                    GLog.i(Messages.get(this, "toohealthy"));
                    detach();
                    return true;
                } else {
                    GLog.i(Messages.get(this, "fallasleep"));
                }
            else if (target instanceof Mob)
                ((Mob) target).state = ((Mob) target).SLEEPING;

            target.paralysed++;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean act() {
        if (target instanceof Hero) {
            target.HP = Math.min(target.HP + 1, target.HT);
            ((Hero) target).resting = true;
            if (target.HP == target.HT) {
                GLog.p(Messages.get(this, "wakeup"));
                detach();
            }
        }
        spend(STEP);
        return true;
    }

    @Override
    public void detach() {
        if (target.paralysed > 0)
            target.paralysed--;
        if (target instanceof Hero)
            ((Hero) target).resting = false;
        super.detach();
    }

    @Override
    public int icon() {
        return BuffIndicator.TIRED;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }
}