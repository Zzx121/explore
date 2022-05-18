local inventoryKeyPrefix = "inventory:"
local userKeyPrefix = "user:"
local marketKey = "market:"

local buyerKey = KEYS[1]
local sellerKey = KEYS[2]
local itemKey = ARGV[1]
local itemPrice = ARGV[2]


--check item exists
local itemRank = redis.call("ZRANK", marketKey, itemKey)
--redis.call("ZADD", marketKey, tostring(itemPrice), itemKey)
redis.call("ZINCRBY", marketKey, 14.3, itemKey)
if itemRank ~= false and itemRank >= 0 then
    local itemPrice = redis.call("ZSCORE", marketKey, itemKey)
    local buyerBalance = redis.call("HGET", userKeyPrefix..buyerKey, "balance")
    if buyerBalance >= itemPrice then
        redis.call("HINCRBY", userKeyPrefix..buyerKey, "balance", -itemPrice)
        redis.call("HINCRBY", userKeyPrefix..sellerKey, "balance", itemPrice)
        redis.call("ZREM", marketKey, itemKey)
        redis.call("SADD", inventoryKeyPrefix..buyerKey, itemKey)

        return "FINISHED"
    end
else
    return nil
end
--check buyer's fund enough
--item from market to inventory
--money from buyer to seller