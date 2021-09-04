package com.pandy.gulimall.product.web;

import com.pandy.gulimall.product.entity.CategoryEntity;
import com.pandy.gulimall.product.service.CategoryService;
import com.pandy.gulimall.product.vo.Catelog2Vo;
import lombok.SneakyThrows;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: IndexController</p>
 * Description：
 * date：2020/6/9 14:01
 */
@Controller
public class IndexController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	RedissonClient redissonClient;

	@Autowired
	StringRedisTemplate redisTemplate;

	@RequestMapping({"/", "index", "/index.html"})
	public String indexPage(Model model) {
		// 获取一级分类所有缓存
		List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
		model.addAttribute("categorys", categorys);
		return "index";
	}

	@SneakyThrows
	@ResponseBody
	@RequestMapping("index/json/catalog.json")
	public Map<String, List<Catelog2Vo>> getCatlogJson() throws InterruptedException {

		Map<String, List<Catelog2Vo>> map = null;
		try {
			map = categoryService.getCatelogJson();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return map;
	}

	@ResponseBody
	@GetMapping("/hello")
	public String hello() {
		// 调用getlock 获取一把锁 锁的名字一样 就是同一把锁
		RLock lock = redissonClient.getLock("my-lock");

		// 加锁
		lock.lock(10, TimeUnit.SECONDS);
		try {
			System.out.println("加锁成功执行业务, 线程为" + Thread.currentThread().getName());
			Thread.sleep(30000);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			System.out.println("释放锁:" + Thread.currentThread().getName());
			lock.unlock();
		}
		return "hello";
	}

	/**
	 * 保证一定能读到最新的数据
	 * 修改期间
	 * 写锁是一个排他锁、读锁是一个共享锁
	 * 只要写锁没有释放 读就必须等待
	 * 读+读   相当于无锁
	 * 写+读   等待写锁释放
	 * 写+写   阻塞方式
	 * 读+写   有读锁 写也需要等待
	 * 只要有写的存在都必须等待
	 *
	 * @return
	 */
	@GetMapping("write")
	@ResponseBody
	public String writeValue() {

		RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
		RLock wlock = readWriteLock.writeLock();

		String s = "";
		try {
			//改数据加写锁 读数据加读锁
			wlock.lock();
			System.out.println("加锁成功执行业务, 线程为" + Thread.currentThread().getName());
			s = UUID.randomUUID().toString();
			Thread.sleep(30000);
			redisTemplate.opsForValue().set("writeValue", s);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			System.out.println("解锁成功执行业务, 线程为" + Thread.currentThread().getName());
			wlock.unlock();
		}

		return s;
	}

	@GetMapping("read")
	@ResponseBody
	public String readValue() {

		RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
		RLock rLock = readWriteLock.readLock();
		String s = "";
		try {
			System.out.println("加锁成功执行业务, 线程为" + Thread.currentThread().getName());
			rLock.lock();
			s = redisTemplate.opsForValue().get("writeValue");
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			System.out.println("解锁成功执行业务, 线程为" + Thread.currentThread().getName());
			rLock.unlock();
		}
		return s;
	}

	/**
	 * 放假 锁门
	 */
	@GetMapping("lockDoor")
	@ResponseBody
	public String lockDoor() throws InterruptedException {

		RCountDownLatch door = redissonClient.getCountDownLatch("door");
		door.trySetCount(5);
		door.await(); // 等待闭锁都完成

		return "放假了";
	}

	@GetMapping("gogogo/{id}")
	public String gogogo(@PathVariable("id") Long id) {

		RCountDownLatch door = redissonClient.getCountDownLatch("door");
		door.countDown();

		return id + "班的人都走了...";
	}
}
