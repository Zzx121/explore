local ownerKey = KEYS[1]
local semaphorePermits = ARGV[1]
local semaphoreMember = ARGV[2]
local timestamp = ARGV[3]
redis.call("ZADD", ownerKey, 'XX', timestamp, semaphoreMember)