# 计划与时间安排 12.16 - 1.18

7个数据源 - 库 - 表

1. 单个数据源，多库多表插入（计划在25号之前完成）
   1. 最简单的方法：读取数据，然后一条条插入
   2. 不知道mysql的插入方法有没有什么优化
2. 多个源，多库多表插入（计划在1.1号之前完成）
   1. 最简单的方法：读取数据，插入时候判断数据库里面是否有主键重复的表
3. 多个数据库插入的优化
   1. 初步优化策略：
      1. 找到每个数据源的相同的数据库以及相同的表，在客户端这里完成数据去重（不知道mysql返回重复的延迟是多少）。
