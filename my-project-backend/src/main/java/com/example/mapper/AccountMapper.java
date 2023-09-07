package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}//这个作用是用来操作数据库的，这里继承了BaseMapper，所以不需要写任何代码，就可以实现增删改查的功能，如果需要自定义的功能，可以在这里写方法，然后在对应的service里面调用
