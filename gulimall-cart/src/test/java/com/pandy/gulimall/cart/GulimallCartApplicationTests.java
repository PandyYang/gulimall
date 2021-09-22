package com.pandy.gulimall.cart;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class GulimallCartApplicationTests {

    @Test
    public void contextLoads() {

        String s = "123";
        int i = Integer.parseInt(s);

        String[] ss = new String[]{"1", "2", "3"};
        List<Integer> collect = Arrays.stream(ss).map(Integer::parseInt).collect(Collectors.toList());
        Integer[] objects = (Integer[])collect.toArray(new Integer[collect.size()]);

    }
}
