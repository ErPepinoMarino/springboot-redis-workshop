local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local ttlSeconds = tonumber(ARGV[3])

local time = redis.call('TIME')
local nowMs = (time[1] * 1000) + math.floor(time[2] / 1000)

local data = redis.call('HMGET', KEYS[1], 'tokens', 'lastRefillMs')
local tokens = tonumber(data[1])
local lastRefillMs = tonumber(data[2])

if tokens == nil then
    tokens = capacity
    lastRefillMs = nowMs
end

local elapsed = (nowMs - lastRefillMs) / 1000.0
tokens = math.min(capacity, tokens + (elapsed * rate))

local allowed = 0
local retryAfterMs = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
elseif rate > 0 then
    retryAfterMs = math.ceil(((1 - tokens) / rate) * 1000)
else
    retryAfterMs = -1
end

redis.call('HSET', KEYS[1], 'tokens', tokens, 'lastRefillMs', nowMs)
redis.call('PEXPIRE', KEYS[1], ttlSeconds * 1000)

return { allowed, math.floor(tokens), retryAfterMs }