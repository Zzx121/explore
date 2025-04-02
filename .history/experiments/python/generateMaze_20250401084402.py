from random import randint
from pathlib import Path
maze_width = int(input("Please set the 【width】 of the maze:"))
maze_height = int(input("Please set the 【height】 of the maze:"))

## make the start and end not on the same border
## generate four border axises    
start_end_list = [(1, randint(1, maze_height)), 
                  (randint(1, maze_width), 1), 
                  (maze_width, randint(1, maze_height)),
                  (randint(1, maze_width), maze_height)]
## better one: 
## 1. find a start point on the borders
start_axis = start_end_list.pop(randint(0, 3))
print(f"start axis: {start_axis}")

## 3. make the distance between start point and end point greater than (width + height / 2)
end_point_list = []
start_x = start_axis[0]
start_y = start_axis[1]
for y in range(1, maze_height + 1):
    for x in range(1, maze_width + 1):
        ## 2. make the end point not in the same x axis and y axis
        if(start_x != x and start_y != y and 
           ## on the borders
           (1 == x or 1 == y or maze_height == y or maze_width == x) and 
           ((abs(start_x - x) ** 2 + abs(start_y - y) ** 2) ** 0.5 > (maze_width + maze_height) / 2)):
            end_point_list.append((x, y))
            
end_point_size = len(end_point_list)
if(end_point_size == 0):
    end_axis = start_end_list.pop(randint(0, 2))
else:
    end_axis = end_point_list.pop(randint(0, len(end_point_list) - 1))
print(f"end axis: {end_axis}")
end_x = end_axis[0]
end_y = end_axis[1]


block = "*"
path = " "
start = "^"
end = "$"
maze_list = []

for y in range(1, maze_height + 1):
    for x in range(1, maze_width + 1):
        if (x == start_x and y == start_y):
            maze_list.append({(x, y): start})
        elif (x == end_x and y == end_y):
            maze_list.append({(x, y): end})
            ## pathes on x
        elif ((y == start_y and x > min(start_x, end_x) and x <= max(start_x, end_x)) or
              ## pathes on y
              (x == end_x and y > min(start_y, end_y) and y < max(start_y, end_y))):
            maze_list.append({(x, y): path})
        else:
            if(randint(1, 10) % 2 == 0):
                maze_list.append({(x, y): block})
            else:
                maze_list.append({(x, y): path})

## write to file
path = Path("maze.txt")
maze_map = ''
lineSize = int(len(maze_list) / maze_width)
count = 0
for m in maze_list:
    maze_map += m.
for l in range(int(len(maze_list) / maze_width)):
    end_slice = l + 1
    
    # print(maze_list[l * maze_width: end_slice * maze_width])
    maze_map += maze_list[l * maze_width: end_slice * maze_width] + '\n'
path.write_text(maze_map)
