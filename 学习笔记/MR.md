#### InputFormat与其实现类TextInputFormat在什么地方使用?

- 1.在client中计数splits切片,也就是计数map的数量
- 2.在mapTsak任务当中使用得到lineRecordreader行记录读取器

