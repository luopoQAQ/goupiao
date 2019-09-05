use goupiao;

CREATE TABLE `train` (
  `train_id` int(11) NOT NULL AUTO_INCREMENT,
  `train_name` varchar(32) DEFAULT NULL,
  `train_type` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`train_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

CREATE TABLE `station` (
  `train_id` int(11) NOT NULL,
  `station_id` int(11) NOT NULL,
  `city_name` varchar(32) DEFAULT NULL,
  `station_name` varchar(32) DEFAULT NULL,
  `arrive_time` char(8) DEFAULT NULL,
  `stop_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`train_id`,`station_id`),
  KEY `city_name` (`city_name`) COMMENT '方便查找'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `seat` (
  `train_id` int(11) NOT NULL,
  `seat_id` int(11) NOT NULL,
  `price` decimal(6,2) DEFAULT NULL,
  `carriage` int(11) DEFAULT NULL,
  `seat_type` varchar(32) DEFAULT NULL,
  `seat_location` char(4) DEFAULT NULL,
  PRIMARY KEY (`train_id`,`seat_id`),
  KEY `seat_type` (`seat_type`) USING BTREE COMMENT '方便查找'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `train_state` (
  `train_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `state` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`train_id`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(32) DEFAULT NULL,
  `password` varchar(32) DEFAULT NULL,
  `id_card` char(18) DEFAULT NULL,
  `state` varchar(32) DEFAULT NULL,
  `salt` char(6) DEFAULT NULL,
  `telephone` char(11) DEFAULT NULL,
  `real_name` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_name` (`user_name`) USING BTREE COMMENT '方便查找、确定用户名唯一性'
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

CREATE TABLE `order_` (
  `order_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `id_card` char(18) DEFAULT NULL,
  `user_name` varchar(32) DEFAULT NULL,
  `train_id` int(11) DEFAULT NULL,
  `train_name` varchar(32) DEFAULT NULL,
  `seat_id` int(11) DEFAULT NULL,
  `price` decimal(6,2) DEFAULT NULL,
  `seat_type` varchar(32) DEFAULT NULL,
  `seat_location` char(4) DEFAULT NULL,
  `from_station_name` varchar(32) DEFAULT NULL,
  `from_station_id` int(11) DEFAULT NULL,
  `to_station_name` varchar(32) DEFAULT NULL,
  `to_station_id` int(11) DEFAULT NULL,
  `from_time` datetime DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `state` varchar(32) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `telephone` char(11) DEFAULT NULL,
  `real_name` varchar(32) DEFAULT NULL,
  `carriage` int(3) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `user_id, date` (`user_id`,`date`,`train_id`) USING BTREE COMMENT '一个用户在同一日只能购买某一车次的一张车票',
  UNIQUE KEY `train_id, seat_id, from_station_id, to_station_id, date` (`train_id`,`seat_id`,`from_station_id`,`to_station_id`,`date`) USING BTREE COMMENT '同一车次某一日的从A到B区间的车票只能被购买一次',
  KEY `from_time` (`from_time`) USING BTREE COMMENT '方便查找'
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

INSERT INTO `train_state` VALUES (1, '2019-8-24', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-25', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-26', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-27', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-28', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-29', '正常');
INSERT INTO `train_state` VALUES (1, '2019-8-30', '停运');
INSERT INTO `train_state` VALUES (1, '2019-8-31', '正常');
INSERT INTO `train_state` VALUES (1, '2019-9-1', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-24', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-25', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-26', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-27', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-28', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-29', '正常');
INSERT INTO `train_state` VALUES (2, '2019-8-30', '停运');
INSERT INTO `train_state` VALUES (2, '2019-8-31', '正常');
INSERT INTO `train_state` VALUES (2, '2019-9-1', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-24', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-25', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-26', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-27', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-28', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-29', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-30', '正常');
INSERT INTO `train_state` VALUES (3, '2019-8-31', '正常');
INSERT INTO `train_state` VALUES (3, '2019-9-1', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-24', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-25', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-26', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-27', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-28', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-29', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-30', '正常');
INSERT INTO `train_state` VALUES (4, '2019-8-31', '正常');
INSERT INTO `train_state` VALUES (4, '2019-9-1', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-24', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-25', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-26', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-27', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-28', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-29', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-30', '正常');
INSERT INTO `train_state` VALUES (5, '2019-8-31', '正常');
INSERT INTO `train_state` VALUES (5, '2019-9-1', '正常');

INSERT INTO `user_` VALUES (5, 'qaqqaq', 'fde235382fedb168a598fd74074ea86b', '342401199806138877', '正常', '613211', '13084041438', '熊宗虎');

INSERT INTO `station` VALUES (1, 0, '北京', '北京', '00:11:55', 0);
INSERT INTO `station` VALUES (1, 1, '郑州', '郑州', '00:23:00', 35);
INSERT INTO `station` VALUES (1, 2, '合肥', '合肥', '01:01:20', 20);
INSERT INTO `station` VALUES (1, 3, '南京', '南京', '01:03:20', 20);
INSERT INTO `station` VALUES (1, 4, '上海', '上海', '01:07:00', 0);
INSERT INTO `station` VALUES (2, 0, '北京', '北京南', '00:10:30', 0);
INSERT INTO `station` VALUES (2, 1, '郑州', '郑州西', '00:14:15', 10);
INSERT INTO `station` VALUES (2, 2, '合肥', '合肥南', '00:17:10', 10);
INSERT INTO `station` VALUES (2, 3, '南京', '南京南', '00:19:30', 15);
INSERT INTO `station` VALUES (2, 4, '上海', '上海虹桥', '00:21:45', 0);
INSERT INTO `station` VALUES (3, 0, '西安', '西安北', '00:09:30', 0);
INSERT INTO `station` VALUES (3, 1, '武汉', '武昌', '00:11:00', 20);
INSERT INTO `station` VALUES (3, 2, '合肥', '合肥南', '00:13:20', 20);
INSERT INTO `station` VALUES (3, 3, '郑州', '郑州南', '00:16:10', 20);
INSERT INTO `station` VALUES (3, 4, '北京', '北京南', '00:17:55', 15);
INSERT INTO `station` VALUES (3, 5, '沈阳', '沈阳北', '00:21:30', 25);
INSERT INTO `station` VALUES (3, 6, '哈尔滨', '哈尔滨东', '00:23:15', 0);
INSERT INTO `station` VALUES (4, 0, '上海', '上海', '00:19:10', 0);
INSERT INTO `station` VALUES (4, 1, '南京', '南京', '00:21:30', 15);
INSERT INTO `station` VALUES (4, 2, '合肥', '合肥', '00:22:50', 25);
INSERT INTO `station` VALUES (4, 3, '六安', '六安', '00:23:30', 15);
INSERT INTO `station` VALUES (4, 4, '武汉', '武汉', '01:02:25', 20);
INSERT INTO `station` VALUES (4, 5, '西安', '西安', '01:06:20', 20);
INSERT INTO `station` VALUES (4, 6, '兰州', '兰州', '01:09:10', 15);
INSERT INTO `station` VALUES (4, 7, '宁夏', '宁夏', '01:11:55', 15);
INSERT INTO `station` VALUES (4, 8, '乌鲁木齐', '乌鲁木齐', '01:16:20', 0);
INSERT INTO `station` VALUES (5, 0, '威海', '威海', '00:08:10', 0);
INSERT INTO `station` VALUES (5, 1, '烟台', '烟台', '00:08:40', 10);
INSERT INTO `station` VALUES (5, 2, '潍坊', '潍坊', '00:09:30', 10);
INSERT INTO `station` VALUES (5, 3, '青岛', '青岛', '00:10:35', 15);
INSERT INTO `station` VALUES (5, 4, '日照', '日照', '00:11:25', 10);
INSERT INTO `station` VALUES (5, 5, '连云港', '连云港', '00:12:55', 10);
INSERT INTO `station` VALUES (5, 6, '宿迁', '宿迁', '00:13:40', 15);
INSERT INTO `station` VALUES (5, 7, '扬州', '扬州', '00:15:05', 25);
INSERT INTO `station` VALUES (5, 8, '南京', '南京', '00:17:25', 20);
INSERT INTO `station` VALUES (5, 9, '芜湖', '芜湖', '00:19:35', 15);
INSERT INTO `station` VALUES (5, 10, '合肥', '合肥', '00:20:20', 10);
INSERT INTO `station` VALUES (5, 11, '六安', '六安', '00:21:05', 10);
INSERT INTO `station` VALUES (5, 12, '阜阳', '阜阳', '00:22:50', 15);
INSERT INTO `station` VALUES (5, 13, '驻马店', '驻马店', '01:00:30', 15);
INSERT INTO `station` VALUES (5, 14, '郑州', '郑州', '01:01:50', 30);
INSERT INTO `station` VALUES (5, 15, '西安', '西安', '01:04:30', 25);
INSERT INTO `station` VALUES (5, 16, '兰州', '兰州', '01:07:20', 15);

INSERT INTO `order_` VALUES (15, 5, '342401199806138877', 'qaqqaq', 1, '1461', 3, 450.00, '软卧', '01下', '北京', 0, '上海', 4, '2019-8-25 11:55:00', '2019-8-24 13:53:19', '正常', '2019-8-25', '13084041438', '熊宗虎', 12);
INSERT INTO `order_` VALUES (16, 5, '342401199806138877', 'qaqqaq', 4, 'D701', 6, 125.00, '二等座', '01A', '合肥', 2, '西安', 5, '2019-8-28 22:50:00', '2019-8-24 15:24:44', '正常', '2019-8-28', '13084041438', '熊宗虎', 2);
INSERT INTO `order_` VALUES (17, 5, '342401199806138877', 'qaqqaq', 5, 'T109', 1, 250.00, '无座', '01A', '日照', 4, '南京', 8, '2019-8-30 11:25:00', '2019-8-24 15:51:11', '正常', '2019-8-30', '13084041438', '熊宗虎', 4);

INSERT INTO `train` VALUES (1, '1461', '其他');
INSERT INTO `train` VALUES (2, 'G127', '高铁');
INSERT INTO `train` VALUES (3, 'G9', '高铁');
INSERT INTO `train` VALUES (4, 'D701', '动车');
INSERT INTO `train` VALUES (5, 'T109', '特快');

INSERT INTO `seat` VALUES (1, 1, 450.00, 12, '软卧', '01上');
INSERT INTO `seat` VALUES (1, 2, 450.00, 12, '软卧', '01中');
INSERT INTO `seat` VALUES (1, 3, 450.00, 12, '软卧', '01下');
INSERT INTO `seat` VALUES (1, 4, 450.00, 12, '软卧', '02上');
INSERT INTO `seat` VALUES (1, 5, 220.00, 2, '硬座', '01A');
INSERT INTO `seat` VALUES (1, 6, 220.00, 2, '硬座', '01B');
INSERT INTO `seat` VALUES (1, 7, 220.00, 2, '硬座', '01C');
INSERT INTO `seat` VALUES (1, 8, 220.00, 2, '硬座', '01D');
INSERT INTO `seat` VALUES (1, 9, 120.00, 5, '无座', '01A');
INSERT INTO `seat` VALUES (1, 10, 120.00, 5, '无座', '01B');
INSERT INTO `seat` VALUES (2, 1, 880.00, 1, '一等座', '01A');
INSERT INTO `seat` VALUES (2, 2, 880.00, 1, '一等座', '01B');
INSERT INTO `seat` VALUES (2, 3, 880.00, 1, '一等座', '01C');
INSERT INTO `seat` VALUES (2, 4, 880.00, 1, '一等座', '01D');
INSERT INTO `seat` VALUES (2, 5, 880.00, 1, '一等座', '01E');
INSERT INTO `seat` VALUES (2, 6, 515.00, 2, '二等座', '01A');
INSERT INTO `seat` VALUES (2, 7, 515.00, 2, '二等座', '01B');
INSERT INTO `seat` VALUES (2, 8, 515.00, 2, '二等座', '01C');
INSERT INTO `seat` VALUES (2, 9, 515.00, 2, '二等座', '01D');
INSERT INTO `seat` VALUES (2, 10, 515.00, 2, '二等座', '01E');
INSERT INTO `seat` VALUES (3, 1, 300.00, 14, '一等座', '01A');
INSERT INTO `seat` VALUES (3, 2, 300.00, 14, '一等座', '01B');
INSERT INTO `seat` VALUES (3, 3, 300.00, 4, '一等座', '05A');
INSERT INTO `seat` VALUES (3, 4, 300.00, 4, '一等座', '05A');
INSERT INTO `seat` VALUES (3, 5, 300.00, 2, '一等座', '06A');
INSERT INTO `seat` VALUES (3, 6, 220.00, 2, '二等座', '01A');
INSERT INTO `seat` VALUES (3, 7, 220.00, 2, '二等座', '01B');
INSERT INTO `seat` VALUES (3, 8, 220.00, 2, '二等座', '01C');
INSERT INTO `seat` VALUES (4, 1, 245.00, 1, '一等座', '01A');
INSERT INTO `seat` VALUES (4, 2, 245.00, 1, '一等座', '01B');
INSERT INTO `seat` VALUES (4, 3, 245.00, 1, '一等座', '01C');
INSERT INTO `seat` VALUES (4, 4, 245.00, 1, '一等座', '01D');
INSERT INTO `seat` VALUES (4, 5, 125.00, 1, '一等座', '01E');
INSERT INTO `seat` VALUES (4, 6, 125.00, 2, '二等座', '01A');
INSERT INTO `seat` VALUES (4, 7, 125.00, 2, '二等座', '01B');
INSERT INTO `seat` VALUES (4, 8, 125.00, 2, '二等座', '01C');
INSERT INTO `seat` VALUES (5, 1, 250.00, 4, '无座', '01A');
