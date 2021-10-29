--local rank = redis.call("ZRANK", "semaphore:remote:owner", "b55c3503-2d53-4220-a55e-3dba750d5e41")
--return rank
return tonumber("20")
--return math.tointeger("20")