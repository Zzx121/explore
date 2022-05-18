--in fact, no need to use lock, the lua script itself is a lock, other script need to wait to execute(no need to use lock)
--and also there's no contention when in exclusive lock(no need to use counter)
--so the timer zremrangebyscore and zinterstore is also can be ignored
local ownerKey = KEYS[1]
local semaphorePermits = ARGV[1]
local semaphoreMember = ARGV[2]
local timestamp = ARGV[3]
local timeout = ARGV[4]
redis.call("ZREMRANGEBYSCORE", timerKey, 10000, (timestamp - timeout))
local card = redis.call("ZCARD", ownerKey)
if card < tonumber(semaphorePermits) then
    redis.call("ZADD", ownerKey, timestamp, semaphoreMember)
    return semaphoreMember
else
    return "EXCEEDED"
end
