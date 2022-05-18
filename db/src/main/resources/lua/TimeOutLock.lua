local lockResult = redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2], 'NX')
if lockResult == 'null' then
    --retry
end
return lockResult