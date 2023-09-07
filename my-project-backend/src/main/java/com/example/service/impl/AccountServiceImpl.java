package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils utils;
    @Resource
    AmqpTemplate amqpTemplate;//

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户or密码错误");
        return User.withUsername(username).password(account.getPassword()).roles(account.getRole()).build();
    }//这里是用来校验用户的，如果用户不存在，就会抛出异常，如果存在，就会返回一个UserDetails对象，这个对象里面包含了用户的信息，包括密码，角色等等

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);//这里是从redis中获取验证码
        if (code == null)
            return "Wrong code!!";
        if (!code.equals(vo.getCode()))
            return "Verify code error";
        return null;
    }//这里是用来校验验证码的，如果验证码正确，就会返回null，如果错误，就会返回一个错误信息

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(vo.getEmail(), vo.getCode()));
        if (verify != null)
            return verify;
        String password = encoder.encode(vo.getPassword());//对密码进行加密
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;

    }

    public Account findAccountByNameOrEmail(String text) {
        return this.query().eq("username", text).or().eq("email", text).one();
    }//这里是用来根据用户名或者邮箱来查找用户的，如果用户不存在，就会返回null，如果存在，就会返回一个Account对象，这个对象里面包含了用户的信息，包括密码，角色等等


    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {// 使用IP地址作为锁对象，确保并发时每个IP只有一个线程进入同步块
            if (!this.verifyLimit(ip)) {
                return "请求过于频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);//这里是将验证码发送到消息队列中，等待消费者消费
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }//这里是将验证码存储到redis中，等待用户验证
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername(); //这里是从前端传过来的数据
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);//这里是从redis中获取验证码
        if (code == null)
            return "Please get verify code first";
        if (!code.equals(vo.getCode()))
            return "Verify code error";
        if (this.existsAccountByEmail(email))
            return "Email already exists";
        if (this.existsAccountByUsername(username))
            return "Username already exists";//并发问题，这里需要加锁
        String password = encoder.encode(vo.getPassword());//对密码进行加密
        Account account = new Account(null, username, password, email, "user", new Date());//这里是创建一个用户对象
        if (this.save(account)) {
            stringRedisTemplate.delete(key);//这里是注册成功后，删除redis中的验证码
            return null;
        }else {
            return "Register failed";
        }
    }//这里是用来注册用户的，如果注册成功，就会返回null，如果失败，就会返回一个错误信息

    private boolean existsAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existsAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }
}
