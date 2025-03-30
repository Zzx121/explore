from random import randint
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
for y in range(1, maze_height + 1):
    for x in range(1, maze_width + 1):
        ## 2. make the end point not in the same x axis and y axis
        if(start_axis[0] != x and start_axis[1] != y and (1 == x or 1 == y or maze_height == y or ma)):
            continue
        elif()




block = "*"
path = " "
start = "^"
end = "$"
maze_list = []

for y in range(1, maze_height + 1):
    for x in range(1, maze_width + 1):
        if (x == start_axis[0] and y == start_axis[1]):
            maze_list.append({(x, y): start})
        elif (x == end_axis[0] and y == end_axis[1]):
            maze_list.append({(x, y): end})
        elif ((y == start_point and x <= maze_width) or
              (x == maze_width and ((y < start_point and y > end_point) 
                                    or (y < end_point and y > start_point)))):
            maze_list.append({(x, y): path})
        else:
            if(randint(1, 10) % 2 == 0):
                maze_list.append({(x, y): block})
            else:
                maze_list.append({(x, y): path})

for l in range(int(len(maze_list) / maze_width)):
    end_slice = l + 1
    print(maze_list[l * maze_width: end_slice * maze_width])
