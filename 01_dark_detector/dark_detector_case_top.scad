// ==============================
// Global parameters
// ==============================

// Wall / floor dimensions
wall_depth     = 2;    // Thickness of surrounding walls
floor_depth    = 2;    // Thickness of base plate
wall_height    = 13;   // Height of walls (currently unused but kept)

// Inner usable area (before walls)
inner_width    = 42;
inner_height   = 32;

// Computed outer dimensions
floor_width    = inner_width  + 2 * wall_depth;
floor_height   = inner_height + 2 * wall_depth;

// Hole parameters
hole_r         = 5;    // Base radius of main hole
hole_fn        = 80;   // Cylinder resolution

// ==============================
// Small top wall feature
// ==============================

top_wall_offset_x = floor_width - 26;
top_wall_width    = 10;
top_wall_height   = 3;

translate([top_wall_offset_x, 0, floor_depth])
    cube([top_wall_width, wall_depth, top_wall_height]);

// ==============================
// Bottom-left hole positioning
// ==============================

bottom_left_x_offset = wall_depth + hole_r;
bottom_left_y_offset = wall_depth + 12 + hole_r;

// Offset for second hole
second_hole_dx = 13;
second_hole_dy = 3;

// ==============================
// Floor plate with holes
// ==============================

difference() {
    // Base plate
    cube([floor_width, floor_height, floor_depth]);

    // Main bottom-left hole
    translate([bottom_left_x_offset, bottom_left_y_offset, 0])
        cylinder(h=floor_depth, r=hole_r, $fn=hole_fn);

    // Secondary hole (slightly larger radius)
    translate([
        bottom_left_x_offset + second_hole_dx,
        bottom_left_y_offset + second_hole_dy,
        0
    ])
        cylinder(h=floor_depth, r=hole_r + 1, $fn=hole_fn);
}

// ==============================
// Vertical sleeve for main hole
// ==============================

main_sleeve_height   = 10;
main_sleeve_thickness = 1;

difference() {
    translate([bottom_left_x_offset, bottom_left_y_offset, 0])
        cylinder(h=main_sleeve_height, r=hole_r, $fn=hole_fn);

    translate([bottom_left_x_offset, bottom_left_y_offset, 0])
        cylinder(h=main_sleeve_height, r=hole_r - main_sleeve_thickness, $fn=hole_fn);
}

// ==============================
// Vertical sleeve for secondary hole
// ==============================

secondary_sleeve_height    = 6;
secondary_sleeve_thickness = 1;

difference() {
    translate([
        bottom_left_x_offset + second_hole_dx,
        bottom_left_y_offset + second_hole_dy,
        0
    ])
        cylinder(h=secondary_sleeve_height, r=hole_r + 1, $fn=hole_fn);

    translate([
        bottom_left_x_offset + second_hole_dx,
        bottom_left_y_offset + second_hole_dy,
        0
    ])
        cylinder(h=secondary_sleeve_height, r=hole_r, $fn=hole_fn);
}
