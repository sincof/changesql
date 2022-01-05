# HikariCP

[Multi-threaded access to the connection pool problems #811](https://github.com/brettwooldridge/HikariCP/issues/811) 

**Multithreaded access to Connections was deprecated in JDBC and is not supported by HikariCP either.** It definitely sounds like multiple threads are interacting with a single Connection instance at once. HikariCP is fast enough that you can obtain a Connection, execute SQL, and then return it back to the pool many times in the course of a request.,

**It is a Best Practice to only hold Connections in local variables, preferably in a try-with-resources block, or possibly passed on the stack, but never in a class member field. If you follow that pattern it is virtually impossible to leak a Connection or accidentally share across threads.**

Not that it should matter, but I'm curious, are you running HikariCP as a fixed size pool, or are you specifying **minimum Idles**?

> 🔢minimumIdle: This property controls the minimum number of idle connections that HikariCP tries to maintain in the pool. If the idle connections dip below this value and total connections in the pool are less than maximumPoolSize, HikariCP will make a best effort to add additional connections quickly and efficiently. However, for maximum performance and responsiveness to spike demands, we recommend not setting this value and instead allowing HikariCP to act as a fixed size connection pool. **Default: same as maximumPoolSize**

通过HikariCP，我们不需要特意的去维护连接池子，从HikariCP获取就行了。