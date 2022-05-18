local lockVal = redis.call('GET', KEYS[1])
if lockVal ~= 'null' and lockVal == ARGV[1] then
    redis.call("DEL", KEYS[1])
else
    return 'null'
end
return lockVal