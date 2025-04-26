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
    # if not in the pathes and not tried four directions, go on
    # the way of match int with the specific next move axis is not flexible
    # next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
    #                       (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
    next_possible_axis = [(current_axis[0] - 1, current_axis[1]), (current_axis[0], current_axis[1] - 1),
                          (current_axis[0] + 1, current_axis[1]), (current_axis[0], current_axis[1] + 1) ]
    moved_list = moved_directions.get(current_axis, [])
    for p in next_possible_axis:
        # priorized go through not moved axis, then go back when needed
        # if((p not in pathes or len(moved_list) == 3) and p not in moved_list):
        if(p not in pathes and p not in moved_list):
            next_axis = p
            # if(len(moved_list) == 3):
            #     pathes.remove(current_axis)
            moved_list.append(next_axis)
            break
    
    # need to go back
    if(current_axis == next_axis):
        for p in reversed(pathes):
            if(p in next_possible_axis and p not in moved_list):
                next_axis = p
                pathes.remove(current_axis)
                moved_list.append(next_axis)
                break
    
    moved_directions[current_axis] = moved_list
    
    next_sign = maze_axis.get(next_axis)
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
        if(next_axis not in pathes):
            pathes.append(next_axis)
        continue
        
print(pathes)
# when there are no left point to move return
# when no road to move just back to previous point to find another direction
# use BFS to find the path
