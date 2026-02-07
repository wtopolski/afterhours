wall_depth = 2;
floor_width = 42 + 2 * wall_depth;
floor_height = 32 + 2 * wall_depth;
floor_depth = 2;
wall_height = 13;
hole_r = 1;

// Floor
cube([floor_width, floor_height, floor_depth]);

// Front wall
translate([0, 0, floor_depth])
difference() {
    h_offset = 5;
    cube([floor_width, wall_depth, wall_height]);
    translate([13, 0, h_offset])
        cube([10, wall_depth, floor_height + wall_height - h_offset]);
}
    
// Back wall
translate([0, floor_height - wall_depth, floor_depth])
    cube([floor_width, wall_depth, wall_height]);
    
// Left wall
translate([0, wall_depth, floor_depth])
    cube([wall_depth, floor_height - 2 * wall_depth, wall_height]);
    
// Right wall
translate([floor_width - wall_depth, wall_depth, floor_depth])
    cube([wall_depth, floor_height - 2 * wall_depth, wall_height]);
    
// Bottom left hole
bottom_left_x_offset = wall_depth + 2 + hole_r;
bottom_left_y_offset = wall_depth + 1 + hole_r;
translate([bottom_left_x_offset, bottom_left_y_offset, floor_depth])
    color("green")
    cylinder(h=wall_height, r=hole_r, $fn=40);
    
// Bottom right hole
bottom_right_x_offset = floor_width - wall_depth - 3 - hole_r;
bottom_right_y_offset = wall_depth + 1 + hole_r;
translate([bottom_right_x_offset, bottom_right_y_offset, floor_depth])
    color("white")
    cylinder(h=wall_height, r=hole_r, $fn=40);
    