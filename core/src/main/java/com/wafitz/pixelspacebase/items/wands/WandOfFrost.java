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
package com.wafitz.pixelspacebase.items.wands;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.actors.Actor;
import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.actors.buffs.Chill;
import com.wafitz.pixelspacebase.actors.buffs.FlavourBuff;
import com.wafitz.pixelspacebase.actors.buffs.Frost;
import com.wafitz.pixelspacebase.effects.MagicMissile;
import com.wafitz.pixelspacebase.items.Heap;
import com.wafitz.pixelspacebase.items.weapon.melee.DM3000Staff;
import com.wafitz.pixelspacebase.levels.Level;
import com.wafitz.pixelspacebase.mechanics.Ballistica;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfFrost extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_FROST;
    }

    public int min(int lvl) {
        return 2 + lvl;
    }

    public int max(int lvl) {
        return 8 + 5 * lvl;
    }

    @Override
    protected void onZap(Ballistica bolt) {

        Heap heap = Dungeon.level.heaps.get(bolt.collisionPos);
        if (heap != null) {
            heap.freeze();
        }

        Char ch = Actor.findChar(bolt.collisionPos);
        if (ch != null) {

            int damage = damageRoll();

            if (ch.buff(Frost.class) != null) {
                return; //do nothing, can't affect a frozen target
            }
            if (ch.buff(Chill.class) != null) {
                //7.5% less damage per turn of chill remaining
                float chill = ch.buff(Chill.class).cooldown();
                damage = (int) Math.round(damage * Math.pow(0.925f, chill));
            } else {
                ch.sprite.burst(0xFF99CCFF, level() / 2 + 2);
            }

            processSoulMark(ch, chargesPerCast());
            ch.damage(damage, this);

            if (ch.isAlive()) {
                if (Level.water[ch.pos])
                    Buff.prolong(ch, Chill.class, 4 + level());
                else
                    Buff.prolong(ch, Chill.class, 2 + level());
            }
        }
    }

    @Override
    protected void fx(Ballistica bolt, Callback callback) {
        MagicMissile.blueLight(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback);
        Sample.INSTANCE.play(Assets.SND_ZAP);
    }

    @Override
    public void onHit(DM3000Staff staff, Char attacker, Char defender, int damage) {
        Chill chill = defender.buff(Chill.class);
        if (chill != null && Random.IntRange(2, 10) > chill.cooldown()) {
            //need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
            new FlavourBuff() {
                {
                    actPriority = Integer.MIN_VALUE;
                }

                public boolean act() {
                    Buff.affect(target, Frost.class, Frost.duration(target) * Random.Float(1f, 2f));
                    return super.act();
                }
            }.attachTo(defender);
        }
    }

    @Override
    public void staffFx(DM3000Staff.StaffParticle particle) {
        particle.color(0x88CCFF);
        particle.am = 0.6f;
        particle.setLifespan(2f);
        float angle = Random.Float(PointF.PI2);
        particle.speed.polar(angle, 2f);
        particle.acc.set(0f, 1f);
        particle.setSize(0f, 1.5f);
        particle.radiateXY(Random.Float(1f));
    }

}
