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
package com.wafitz.pixelspacebase.ui;

import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

class Compass extends Image {

    private static final float RAD_2_G = 180f / 3.1415926f;
    private static final float RADIUS = 12;

    private int cell;
    private PointF cellCenter;

    private PointF lastScript = new PointF();

    Compass(int cell) {

        super();
        copy(Icons.COMPASS.get());
        origin.set(width / 2, RADIUS);

        this.cell = cell;
        cellCenter = DungeonTilemap.tileCenterToWorld(cell);
        visible = false;
    }

    @Override
    public void update() {
        super.update();

        if (!visible) {
            visible = Dungeon.level.visited[cell] || Dungeon.level.mapped[cell];
        }

        if (visible) {
            PointF script = Camera.main.script;
            if (!script.equals(lastScript)) {
                lastScript.set(script);
                PointF center = Camera.main.center().offset(script);
                angle = (float) Math.atan2(cellCenter.x - center.x, center.y - cellCenter.y) * RAD_2_G;
            }
        }
    }
}
