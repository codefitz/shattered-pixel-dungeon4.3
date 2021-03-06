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
package com.wafitz.pixelspacebase.levels;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.DungeonTilemap;
import com.wafitz.pixelspacebase.actors.mobs.npcs.Hologram;
import com.wafitz.pixelspacebase.effects.Ripple;
import com.wafitz.pixelspacebase.items.AirTank;
import com.wafitz.pixelspacebase.items.Generator;
import com.wafitz.pixelspacebase.items.Heap;
import com.wafitz.pixelspacebase.items.WeakForcefield;
import com.wafitz.pixelspacebase.items.armor.SpaceSuit;
import com.wafitz.pixelspacebase.items.armor.Uniform;
import com.wafitz.pixelspacebase.items.food.Food;
import com.wafitz.pixelspacebase.items.scripts.MappingScript;
import com.wafitz.pixelspacebase.levels.vents.AlarmVent;
import com.wafitz.pixelspacebase.levels.vents.ChillingVent;
import com.wafitz.pixelspacebase.levels.vents.FlockVent;
import com.wafitz.pixelspacebase.levels.vents.OozeVent;
import com.wafitz.pixelspacebase.levels.vents.SummoningVent;
import com.wafitz.pixelspacebase.levels.vents.TeleportationVent;
import com.wafitz.pixelspacebase.levels.vents.ToxicVent;
import com.wafitz.pixelspacebase.levels.vents.WornVent;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class OperationsLevel extends RegularLevel {

    {
        color1 = 0x48763c;
        color2 = 0x59994a;
    }

    @Override
    public String tilesTex() {
        return Assets.TILES_OPERATIONS;
    }

    @Override
    public String waterTex() {
        return Assets.WATER_OPERATIONS;
    }

    protected boolean[] water() {
        return Patch.generate(this, feeling == Feeling.WATER ? 0.60f : 0.45f, 5);
    }

    protected boolean[] lightedvent() {
        return Patch.generate(this, feeling == Feeling.LIGHTEDVENT ? 0.60f : 0.40f, 4);
    }

    @Override
    protected Class<?>[] ventClasses() {
        return Dungeon.depth == 1 ?
                new Class<?>[]{WornVent.class} :
                new Class<?>[]{ChillingVent.class, ToxicVent.class, WornVent.class,
                        AlarmVent.class, OozeVent.class,
                        FlockVent.class, SummoningVent.class, TeleportationVent.class,};
    }

    @Override
    protected float[] ventChances() {
        return Dungeon.depth == 1 ?
                new float[]{1} :
                new float[]{4, 4, 4,
                        2, 2,
                        1, 1, 1};
    }

    @Override
    protected void decorate() {

        for (int i = 0; i < width(); i++) {
            if (map[i] == Terrain.WALL &&
                    map[i + width()] == Terrain.WATER &&
                    Random.Int(4) == 0) {

                map[i] = Terrain.WALL_DECO;
            }
        }

        for (int i = width(); i < length() - width(); i++) {
            if (map[i] == Terrain.WALL &&
                    map[i - width()] == Terrain.WALL &&
                    map[i + width()] == Terrain.WATER &&
                    Random.Int(2) == 0) {

                map[i] = Terrain.WALL_DECO;
            }
        }

        for (int i = width() + 1; i < length() - width() - 1; i++) {
            if (map[i] == Terrain.EMPTY) {

                int count =
                        (map[i + 1] == Terrain.WALL ? 1 : 0) +
                                (map[i - 1] == Terrain.WALL ? 1 : 0) +
                                (map[i + width()] == Terrain.WALL ? 1 : 0) +
                                (map[i - width()] == Terrain.WALL ? 1 : 0);

                if (Random.Int(16) < count * count) {
                    map[i] = Terrain.EMPTY_DECO;
                }
            }
        }

        //hides all doors in the entrance room on floor 2, teaches the player to search.
        if (Dungeon.depth == 2)
            for (Room r : roomEntrance.connected.keySet()) {
                Room.Door d = roomEntrance.connected.get(r);
                if (d.type == Room.Door.Type.REGULAR)
                    map[d.x + d.y * width()] = Terrain.SECRET_DOOR;
            }

        placeSign();

        // wafitz.v1 - Hero belongings are now to be found in the entrance, later I will randomly place this somewhere on the level
        if (Dungeon.depth <= 1) {
                int pos = pointToCell(roomEntrance.random());
            if (pos != entrance && vents.get(pos) == null
                        && findMob(pos) == null && pos != Terrain.SIGN) {
                    drop(Generator.random(), pos).type = Heap.Type.CHEST;
                    drop(new Uniform().identify(), pos);
                    drop(new Food().identify(), pos);
                    // Testing
                drop(new MappingScript().identify(), pos);
                //drop(new DeviceCase().identify(), pos);
                //drop(new BlasterHolster().identify(), pos);
                //drop(new XPort().identify(), pos);
                drop(new WeakForcefield().identify(), pos);
                drop(new SpaceSuit().identify(), pos);
                }
        }
    }

    @Override
    protected void createItems() {
        if (!Dungeon.limitedDrops.airTank.dropped() && Random.Int(4 - Dungeon.depth) == 0) {
            addItemToSpawn(new AirTank());
            Dungeon.limitedDrops.airTank.drop();
        }

        Hologram.Quest.spawn(this);

        super.createItems();
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        addSewerVisuals(this, visuals);
        return visuals;
    }

    static void addSewerVisuals(Level level, Group group) {
        for (int i = 0; i < level.length(); i++) {
            if (level.map[i] == Terrain.WALL_DECO) {
                // wafitz.v4: Lights don't leak!
                //group.add(new Sink(i));
            }
        }
    }

    @Override
    public String tileName(int tile) {
        switch (tile) {
            case Terrain.WATER:
                return Messages.get(OperationsLevel.class, "water_name");
            default:
                return super.tileName(tile);
        }
    }

    @Override
    public String tileDesc(int tile) {
        switch (tile) {
            case Terrain.EMPTY_DECO:
                return Messages.get(OperationsLevel.class, "empty_deco_desc");
            case Terrain.BOOKSHELF:
                return Messages.get(OperationsLevel.class, "bookshelf_desc");
            default:
                return super.tileDesc(tile);
        }
    }

    private static class Sink extends Emitter {

        private int pos;
        private float rippleDelay = 0;

        private static final Emitter.Factory factory = new Factory() {

            @Override
            public void emit(Emitter emitter, int index, float x, float y) {
                WaterParticle p = (WaterParticle) emitter.recycle(WaterParticle.class);
                p.reset(x, y);
            }
        };

        public Sink(int pos) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld(pos);
            pos(p.x - 2, p.y + 1, 4, 0);

            pour(factory, 0.1f);
        }

        @Override
        public void update() {
            if (visible = Dungeon.visible[pos]) {

                super.update();

                if ((rippleDelay -= Game.elapsed) <= 0) {
                    Ripple ripple = GameScene.ripple(pos + Dungeon.level.width());
                    if (ripple != null) {
                        ripple.y -= DungeonTilemap.SIZE / 2;
                        rippleDelay = Random.Float(0.4f, 0.6f);
                    }
                }
            }
        }
    }

    private static final class WaterParticle extends PixelParticle {

        public WaterParticle() {
            super();

            acc.y = 50;
            am = 0.5f;

            color(ColorMath.random(0xb6ccc2, 0x3b6653));
            size(2);
        }

        public void reset(float x, float y) {
            revive();

            this.x = x;
            this.y = y;

            speed.set(Random.Float(-2, +2), 0);

            left = lifespan = 0.5f;
        }
    }
}
