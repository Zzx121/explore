local ownerKey = KEYS[1]
local counterKey = KEYS[2]
local timerKey = KEYS[3]
local semaphorePermits = ARGV[1]
local semaphoreMember = ARGV[2]
local timestamp = ARGV[3]
local timeout = ARGV[4]
--local timestamp = os.time()
--get the counter
--store the counter as semaphore score
--store current time as time zset's score
--remove range by score of the timer zset
--zinterstore of two zset and filter out timed out items
--check rank to filter out exceeded items
--clean jobs
local counter = redis.call("INCR", counterKey)
redis.call("ZADD", ownerKey, counter, semaphoreMember)
redis.call("ZADD", timerKey, timestamp, semaphoreMember)             
redis.call("ZREMRANGEBYSCORE", timerKey, 10000, (timestamp - timeout))
redis.call("ZINTERSTORE", ownerKey, 2, ownerKey, timerKey, 'WEIGHTS', 1, 0, 'AGGREGATE', 'SUM')
local rank = redis.call("ZRANK", ownerKey, semaphoreMember)
if rank < tonumber(semaphorePermits) then
    return "OK"
else
    redis.call("ZREM", ownerKey, semaphoreMember)
    redis.call("ZREM", timerKey, semaphoreMember)
    return "EXCEEDED"
end
