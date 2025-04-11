# read the maze into list with axis, map is more suitable
lineCount = 1
maze_axis = {}
block = "*"
path = " "
start = "^"
end = "$"
with open("maze.txt") as file:
    for line in file:
        # the original rstrip will also strip the normal ' '
        for idx, p in enumerate(line.rstrip("\n")):
            maze_axis[(idx + 1, lineCount)]= p
            if(p == start):
                start_axis = (idx + 1, lineCount)
            elif(p == end):
                end_axis = (idx + 1, lineCount)
        lineCount += 1
# use DFS to find the path
pathes = [start_axis]
current_axis = start_axis
moved_directions = {}
# move from the start
while(True):
    moved_count = moved_directions.get(current_axis, 0)
    # if not in the pathes and not tried four directions, go on
    # the way of match int with the specific next move axis is not flexible
    next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
                          (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
    can_move_axis = []
    moved_list = moved_directions.get(current_axis)
    if(moved_list == None):
        moved_list = []
    for p in next_possible_axis:
        if(p not in pathes and p not in moved_list):
            next_axis = p
            break
    
    next_axis = (next_x, next_y)
    next_sign = maze_axis.get(next_axis)
    # construct to map with list values
    moved_list = moved_directions.get(next_axis)
    if(moved_list == None):
        moved_list = []
    moved_list.append(next_axis)
    moved_directions[next_axis] = moved_list
    # when meet the end just return
    if(next_axis == end_axis):
        pathes.append(next_axis)
        break
    # meet the block or out of range
    elif(next_sign == None or next_sign == block):
        next_axis = current_axis
        continue
    elif(next_sign == path):
        current_axis = next_axis
        pathes.append(next_axis)
        continue
        
print(pathes)
# when there are no left point to move return
# when no road to move just back to previous point to find another direction
# use BFS to find the path
