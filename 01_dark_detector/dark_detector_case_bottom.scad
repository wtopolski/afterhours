// ==============================
// Global parameters
// ==============================

// Wall / floor thickness
wall_depth     = 2;    // Thickness of walls
floor_depth    = 2;    // Thickness of floor plate
wall_height    = 13;   // Height of all vertical walls

// Inner usable dimensions
inner_width    = 42;
inner_height   = 32;

// Computed outer dimensions
floor_width    = inner_width  + 2 * wall_depth;
floor_height   = inner_height + 2 * wall_depth;

// Hole parameters
hole_r         = 1;    // Radius of small mounting holes
hole_fn        = 40;   // Cylinder resolution

// ==============================
// Floor plate
// ==============================

cube([floor_width, floor_height, floor_depth]);

// ==============================
// Front wall with vertical cutout
// ==============================

front_wall_cutout_x        = 13;   // X offset of cutout
front_wall_cutout_width   = 10;   // Width of cutout
front_wall_cutout_z_start = 5;    // Bottom offset of cutout

translate([0, 0, floor_depth])
difference() {

    // Full front wall
    cube([floor_width, wall_depth, wall_height]);

    // Vertical opening in front wall
    translate([front_wall_cutout_x, 0, front_wall_cutout_z_start])
        cube([
            front_wall_cutout_width,
            wall_depth,
            floor_height + wall_height - front_wall_cutout_z_start
        ]);
}

// ==============================
// Back wall
// ==============================

translate([0, floor_height - wall_depth, floor_depth])
    cube([floor_width, wall_depth, wall_height]);

// ==============================
// Left wall
// ==============================

translate([0, wall_depth, floor_depth])
    cube([
        wall_depth,
        floor_height - 2 * wall_depth,
        wall_height
    ]);

// ==============================
// Right wall
// ==============================

translate([floor_width - wall_depth, wall_depth, floor_depth])
    cube([
        wall_depth,
        floor_height - 2 * wall_depth,
        wall_height
    ]);

// ==============================
// Bottom mounting holes
// ==============================

// Vertical offset from inner wall
hole_y_offset = wall_depth + 1 + hole_r;

// Left hole positioning
left_hole_x_offset = wall_depth + 2 + hole_r;

// Right hole positioning
right_hole_x_offset = floor_width - wall_depth - 3 - hole_r;

// Bottom-left hole
translate([left_hole_x_offset, hole_y_offset, floor_depth])
    color("green")
    cylinder(h=wall_height, r=hole_r, $fn=hole_fn);

// Bottom-right hole
translate([right_hole_x_offset, hole_y_offset, floor_depth])
    color("white")
    cylinder(h=wall_height, r=hole_r, $fn=hole_fn);
