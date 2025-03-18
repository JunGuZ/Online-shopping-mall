package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.mapper.*;
import com.example.utils.TokenUtils;
import com.example.utils.UserCF;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import jdk.nashorn.internal.ir.LiteralNode;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.management.relation.Role;
import java.lang.reflect.Executable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 公告信息表业务处理
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CollectMapper collectMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private PetsMapper petsMapper;

    /**
     * 新增
     */
    public void add(Goods goods) {

        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
       goods.setBusinessId(currentUser.getId());
        }

        goodsMapper.insert(goods);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            goodsMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
    }

    /**
     * 根据ID查询
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * 分页查询
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }
    public List<Goods>selectTop15() {

        return goodsMapper.selectTop15();
    }
    public List<Goods> selectByTypeId(Integer id) {
        return goodsMapper.selectByTypeId(id);
    }
    public List<Goods> selectByBusinessId(Integer id) {
        return goodsMapper.selectByBusinessId(id);
    }
    public List<Goods> selectByName(String name) {
        return goodsMapper.selectByName(name);
    }


    // 推荐
    public List<Goods> recommend() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (ObjectUtil.isEmpty(currentUser)) {
            // 没有用户登录
            return new ArrayList<>();
        }
        // 用户的哪些行为可以认为他跟商品产生了关系？收藏、加入购物车、下单、评论
        // 1. 获取所有的收藏信息
        List<Collect> allCollects = collectMapper.selectAll(null);
        // 2. 获取所有的购物车信息
        List<Cart> allCarts = cartMapper.selectAll(null);
        // 3. 获取所有的订单信息
        List<Orders> allOrders = ordersMapper.selectAllOKOrders();
        // 4. 获取所有的评论信息
        List<Comment> allComments = commentMapper.selectAll(null);
        // 5. 获取所有的用户信息
        List<User> allUsers = userMapper.selectAll(null);
        // 6. 获取所有的商品信息
        List<Goods> allGoods = goodsMapper.selectAll(null);

        System.out.println("c1: " + allCollects.toString());
        System.out.println("c2: " + allCarts.toString());
        System.out.println("c3: " + allOrders.toString());
        System.out.println("c4: " + allComments.toString());
        System.out.println("c5: " + allUsers.toString());
        System.out.println("c6: " + allGoods.toString());
        // 定义一个存储每个商品和每个用户关系的List
        List<RelateDTO> data = new ArrayList<>();
        // 定义一个存储最后返回给前端的商品List
        List<Goods> recommendResult;
        //创建一个栅栏，等待所有的异步处理都结束后，再往下走
        CountDownLatch  countDownLatch = new CountDownLatch(allCollects.size() * allUsers.size());
        //创建一个线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();

        // 开始计算每个商品和每个用户之间的关系数据  按照时间衰减来分配权重
        for (Goods goods : allGoods) {
            Integer goodsId = goods.getId();
            for (User user : allUsers) {
                threadPool.execute(() -> {
                    Integer userId = user.getId();
                    int index = 0; // 初始权重为0

                    // 1. 判断该用户有没有收藏该商品
                    // 收藏的权重我们给 1，近七天给 2，近三天给 3 （不叠加）
                    Optional<Collect> collectOptional = allCollects.stream()
                            .filter(x -> x.getGoodsId().equals(goodsId) && x.getUserId().equals(userId))
                            .findFirst();
                    if (collectOptional.isPresent()) {
                        Date updateTime = collectOptional.get().getUpdateTime();
                        index += getTimeDecayWeight(updateTime, 1, 2, 3);
                    }

                    // 2. 判断该用户有没有给该商品加入购物车，
                    // 加入购物车的权重我们给 2，近七天给 4，近三天给 6 （不叠加）
                    Optional<Cart> cartOptional = allCarts.stream()
                            .filter(x -> x.getGoodsId().equals(goodsId) && x.getUserId().equals(userId))
                            .findFirst();
                    if (cartOptional.isPresent()) {
                        Date updateTime = cartOptional.get().getUpdateTime();
                        index += getTimeDecayWeight(updateTime, 2, 4, 6);
                    }

                    // 3. 判断该用户有没有对该商品下过单（已完成的订单）
                    // 订单的权重我们给 3，近七天给 6，近三天给 9 （不叠加）
                    Optional<Orders> ordersOptional = allOrders.stream()
                            .filter(x -> x.getGoodsId().equals(goodsId) && x.getUserId().equals(userId))
                            .findFirst();
                    if (ordersOptional.isPresent()) {
                        Date updateTime = ordersOptional.get().getUpdateTime();
                        index += getTimeDecayWeight(updateTime, 3, 6, 9);
                    }

                    // 4. 判断该用户有没有对该商品评论过
                    // 评论的权重我们给 2，，近七天给 4，近三天给 6 （不叠加）
                    Optional<Comment> commentOptional = allComments.stream()
                            .filter(x -> x.getGoodsId().equals(goodsId) && x.getUserId().equals(userId))
                            .findFirst();
                    if (commentOptional.isPresent()) {
                        Date updateTime = commentOptional.get().getUpdateTime();
                        index += getTimeDecayWeight(updateTime, 2, 4, 6);
                    }

                    if (index > 0) {
                        RelateDTO relateDTO = new RelateDTO(userId, goodsId, index);
                        data.add(relateDTO);
                    }
                    countDownLatch.countDown();
                });
            }
        }


        try {
            threadPool.shutdown();
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("user1: " + currentUser.getId());

            // 数据准备结束后，就把这些数据一起喂给协同过滤推荐算法
            List<Integer> goodsIds = UserCF.recommend(currentUser.getId(), data);


            // 把商品id转换成商品
             recommendResult = goodsIds.stream().map(goodsId -> allGoods.stream()
                            .filter(x -> x.getId().equals(goodsId)).findFirst().orElse(null))
                    .limit(5).collect(Collectors.toList());
        }


        System.out.println("re1: " + recommendResult);
        if (recommendResult.size() < 5) {
            int num = 5 - recommendResult.size();
            List<Goods> list = getRandomGoods(num);
            recommendResult.addAll(list);
        }
        // List<Goods> recommendResult;
        System.out.println("re2: " + recommendResult);

        List<Pets> allPets = petsMapper.selectAll(null);

        List<String> petsKeyWords = new ArrayList<>();
        if (allPets != null) {
            for (Pets pets : allPets) {
                petsKeyWords.add(pets.getPetName());
            }
        }


        // 1. 将商品分为含有关键词和不含关键词的两组（一次性遍历，效率更高）
        Map<Boolean, List<Goods>> partitionedGoods = recommendResult.stream()
                .collect(Collectors.partitioningBy(
                        goods -> petsKeyWords.stream()
                                .anyMatch(keyword -> goods.getName().contains(keyword))
                ));

        // 2. 对两组分别按更新时间倒序排序
        Comparator<Goods> timeComparator = Comparator.comparing(Goods::getUpdateTime).reversed();

        List<Goods> matchedGoods = partitionedGoods.get(true).stream()
                .sorted(timeComparator)
                .collect(Collectors.toList());

        List<Goods> unmatchedGoods = partitionedGoods.get(false).stream()
                .sorted(timeComparator)
                .collect(Collectors.toList());

        // 3. 合并结果（匹配组在前）
        List<Goods> finalResult = new ArrayList<>();
        finalResult.addAll(matchedGoods);
        finalResult.addAll(unmatchedGoods);

        // 最终将结果赋回 recommendResult
        recommendResult = finalResult;


        return recommendResult;
    }

    // 辅助方法：根据时间衰减计算权重
    private int getTimeDecayWeight(Date updateTime, int baseWeight, int sevenDaysWeight, int threeDaysWeight) {
        Date now = new Date(); // 当前时间
        long timeDifference = now.getTime() - updateTime.getTime(); // 时间差（毫秒）
        long threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L; // 3天的毫秒数
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L; // 7天的毫秒数

        if (timeDifference <= threeDaysInMillis) {
            return threeDaysWeight;
        } else if (timeDifference <= sevenDaysInMillis) {
            return sevenDaysWeight;
        } else {
            return baseWeight;
        }
    }

    // 随机前15销量的商品，确保不重复
    private List<Goods> getRandomGoods(int num) {
        List<Goods> goods = goodsMapper.selectTop15(); // 获取前15销量的商品
        if (num > goods.size()) {
            throw new IllegalArgumentException("请求的数量不能超过商品总数");
        }

        Set<Goods> selectedGoods = new HashSet<>(); // 用于存储已选中的商品
        Random random = new Random();

        while (selectedGoods.size() < num) {
            int index = random.nextInt(goods.size()); // 随机生成索引
            selectedGoods.add(goods.get(index)); // 添加到集合中（自动去重）
        }

        return new ArrayList<>(selectedGoods); // 将集合转换为列表返回
    }
}