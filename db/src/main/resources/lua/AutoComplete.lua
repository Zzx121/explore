local memberPrefix = "member:"
local guildKey = KEYS[1]
local keyWord = KEYS[2]
local identifier = ARGV[1]

--get start and end border of the keyWord in dictionary


local function getStartAndEndOfKeyWordInDic(keyWord)
    local dictionaryLetters = "`abcdefghijklmnopqrstuvwxyz{"
    keyWord = string.lower(keyWord)
    local len = string.len(keyWord)
    if len == 0 then
        return {}
    end
    local lastWord = string.sub(keyWord, len, len)
    local predecessorLetterIndex = string.find(dictionaryLetters, lastWord) - 1
    local predecessorLetter = string.sub(dictionaryLetters, predecessorLetterIndex, predecessorLetterIndex)
    local wordPrefix = string.sub(keyWord, 0, len - 1)
    return {wordPrefix..predecessorLetter.."{", keyWord.."{"}
end

local startAndEndTable = getStartAndEndOfKeyWordInDic(keyWord)
local startWord = startAndEndTable[1]
local endWord = startAndEndTable[2]
local memberKey = memberPrefix..guildKey
local startBorder = startWord..identifier
local endBorder = endWord..identifier
redis.call("ZADD", memberKey, 0, startBorder, 0, endBorder)
local startRank = redis.call("ZRANK", memberKey, startBorder)
local endRank = redis.call("ZRANK", memberKey, endBorder)
redis.call("ZREM", memberKey, startBorder, endBorder)

--return at most 10 items
local limitedEndRank = math.min(startRank + 9, endRank - 2)
return redis.call("ZRANGE", memberKey, startRank, limitedEndRank)
--return tostring(startRank)
