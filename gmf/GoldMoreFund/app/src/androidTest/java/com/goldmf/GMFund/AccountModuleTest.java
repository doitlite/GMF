package com.goldmf.GMFund;

import android.test.AndroidTestCase;

/**
 * Created by yale on 15/7/30.
 */
public class AccountModuleTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

//
//    public void testVerifyPhone() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        MineManager.getInstance().verifyPhone("18033441052", new MResults<JsonObject>() {
//            @Override
//            public void onResult(MResultsInfo<JsonObject> result) {
//                assertEquals(true, result.isSuccess);
//                latch.countDown();
//            }
//        });
//        latch.await();
//    }

//    public void testVerifyPhone() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        StockManager.getInstance().searchKey("12", new MResults<List<StockBrief>>() {
//            @Override
//            public void onResult(MResultsInfo<List<StockBrief>> result) {
//                assertEquals(true, result.isSuccess);
//                assertTrue(result.data.size() != 0);
//                latch.countDown();
//            }
//        });
//        latch.await();
//    }
//
//    public void testGetStockInfo() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        StockBrief sstock = new StockBrief();
//        sstock.code = "399992";
//        sstock.type = "SZ";
//        StockManager.getInstance().getStockInfo(sstock,
//                new MResults<Stock>() {
//                    @Override
//                    public void onResult(MResultsInfo<Stock> result) {
//                        assertEquals(true, result.isSuccess);
//                        assertTrue(result.data.last != 0);
//                        latch.countDown();
//                    }
//                });
//        latch.await();
//    }
//
//    public void testModifyPassword() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        MineManager.getInstance().ModifyPassword(true,
//                "ssss",
//                "bbb",
//                "bbb", new MResults<Void>() {
//
//                    @Override
//                    public void onResult(MResultsInfo<Void> result) {
//                        assertEquals(true, result.isSuccess);
//                        latch.countDown();
//
//                    }
//                });
//        latch.await();
//    }
//
//    public void testGetFundBrief() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        TraderManager.getInstance().getFundBrief(453355,
//                FundBrief.Fund_Type.Porfolio,
//                new MResults<FundBrief>() {
//
//                    @Override
//                    public void onResult(MResultsInfo<FundBrief> result) {
//                        assertEquals(true, result.isSuccess);
//                        latch.countDown();
//
//                    }
//                });
//        latch.await();
//    }
//
//    public void testGetPortfolio() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        TraderManager.getInstance().getPortfolio(453355,
//                new MResults<Portfolio>() {
//
//                    @Override
//                    public void onResult(MResultsInfo<Portfolio> result) {
//                        assertEquals(true, result.isSuccess);
//                        latch.countDown();
//
//                    }
//                });
//        latch.await();
//    }


//    public void testFormat() throws Exception {
//        assertEquals(FormatUtil.formatMoney(12, true), "12");
//        assertEquals(FormatUtil.formatMoney(123, true), "123");
//        assertEquals(FormatUtil.formatMoney(1234, true), "1,234");
//        assertEquals(FormatUtil.formatMoney(12345678, true), "12,345,678");
//
//        assertEquals(FormatUtil.formatMoney(-12, true), "-12");
//        assertEquals(FormatUtil.formatMoney(-123, true), "-123");
//        assertEquals(FormatUtil.formatMoney(-1234, true), "-1,234");
//        assertEquals(FormatUtil.formatMoney(-12345678, true), "-12,345,678");
//
//        assertEquals(FormatUtil.formatMoney(0.12, true), "0.12");
//        assertEquals(FormatUtil.formatMoney(0.1234, true), "0.12");
//        assertEquals(FormatUtil.formatMoney(0.123456, true), "0.12");
//
//        assertEquals(FormatUtil.formatMoney(1.1234, true), "1.12");
//        assertEquals(FormatUtil.formatMoney(12.1234, true), "12.12");
//        assertEquals(FormatUtil.formatMoney(123.1234, true), "123.12");
//
//        String str1= FormatUtil.formatMoney(1234.1234f, true);
//
//        assertEquals(FormatUtil.formatMoney(1234.1234, true), "+1,234.12");
//
//        String str2= FormatUtil.formatMoney(1234567.1234, true);
//
//        assertEquals(FormatUtil.formatMoney(12345678.1234, true), "+12,345,678.12");
//        assertEquals(FormatUtil.formatMoney(12345678.12345678f, true), "+12,345,678.12");
//
//        assertEquals(FormatUtil.formatMoney(-1.1234, true), "-1.12");
//        assertEquals(FormatUtil.formatMoney(-12.1234, true), "-12.12");
//        assertEquals(FormatUtil.formatMoney(-123.1234, true), "-123.12");
//        assertEquals(FormatUtil.formatMoney(-1234.1234, true), "-1,234.12");
//        assertEquals(FormatUtil.formatMoney(-12345678.1234, true), "-12,345,678.12");
//        assertEquals(FormatUtil.formatMoney(-12345678.12345678, true), "-12,345,678.12");
//    }

//    public  void  testTime() throws  Exception{
//        long time = new Date().getTime();
//        time -= 2 * 1000;
//        String str1 = FormatUtil.formatTimeByNow(time);
//
//        time -= 5 * 60 * 1000;
//        String str2 = FormatUtil.formatTimeByNow(time);
//
//        time -= 60 * 60 * 1000;
//        String str3 = FormatUtil.formatTimeByNow(time);
//
//        time -= 16* 60 * 60 * 1000;
//        String str4 = FormatUtil.formatTimeByNow(time);
//
//        time -= 24 * 60 * 60 * 1000;
//        String str5 = FormatUtil.formatTimeByNow(time);
//
//        time -= 2* 24 * 60 * 60 * 1000;
//        String str6 = FormatUtil.formatTimeByNow(time);
//
//        time -= 4 * 24 * 60 * 60 * 1000;
//        String str7 = FormatUtil.formatTimeByNow(time);
//
//        time -= (long)250 * 24 * 60 * 60 * 1000;
//        String str8 = FormatUtil.formatTimeByNow(time);
//
//
//        time -= 4* 24 * 60 * 60 * 1000;
//    }

//    public void testVerifyPhone() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        MineManager.getInstance().verifyPhone("18033441052", new MResults<JsonObject>() {
//            @Override
//            public void onResult(MResultsInfo<JsonObject> result) {
//                assertEquals(true, result.isSuccess);
//                latch.countDown();
//            }
//        });
//        latch.await();
//    }

//    public void testGetFocusList() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        DiscoverManager.getInstance().freshFocusList(new MResults<List<FocusInfo>>() {
//            @Override
//            public void onResult(MResultsInfo<List<FocusInfo>> result) {
//                assertEquals(true, result.isSuccess);
//                latch.countDown();
//
//            }
//        });
//        latch.await();
//    }
//
//    public void testRecommandFundList() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        DiscoverManager.getInstance().freshRecommandFundList(new MResults<List<FundBrief>>() {
//            @Override
//            public void onResult(MResultsInfo<List<FundBrief>> result) {
//                assertEquals(true, result.isSuccess);
//                latch.countDown();
//
//            }
//        });
//        latch.await();
//    }
//
//    public void testRecommandTraderList() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//        DiscoverManager.getInstance().freshRecommandTraderList(new MResults<List<User>>() {
//            @Override
//            public void onResult(MResultsInfo<List<User>> result) {
//                assertEquals(true, result.isSuccess);
//                latch.countDown();
//
//            }
//        });
//        latch.await();
//    }

//    public void testKline() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        StockInfo.StockSimple sstock = new StockInfo.StockSimple();
//        sstock.index = "000001.SZ";
//        StockManager.getInstance().getStockKLine(sstock,
//                LineData.GMFStockAnswer_Authority_No,
//                LineData.KLineData.Kline_Type_Day, result -> {
//                    assertEquals(true, result.isSuccess);
//                }
//        );
//        latch.await();
//    }

//    public void testLogin() throws Exception {
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        MineManager.getInstance().login(MineManager.PhoneLogin, "18938670611", "leijingjing", new MResults<User>() {
//            @Override
//            public void onResult(MResultsInfo<User> result) {
//                assertEquals(true, result.isSuccess);
//
//                FortuneManager.getInstance().freshAccount(new MResults<Void>() {
//                    @Override
//                    public void onResult(MResultsInfo<Void> result) {
//                        assertEquals(true, result.isSuccess);
//                    }
//                });
//
//                FortuneManager.getInstance().freshHoldFunds(1, new MResults<FortuneInfo>() {
//                    @Override
//                    public void onResult(MResultsInfo<FortuneInfo> result) {
//                        assertEquals(true, result.isSuccess);
//                    }
//                });
//
//                FortuneManager.getInstance().freshAccountTraderList(new MResults<List<AccountTradeInfo>>() {
//                    @Override
//                    public void onResult(MResultsInfo<List<AccountTradeInfo>> result) {
//                        assertEquals(true, result.isSuccess);
//                    }
//                });
//
//
//                CashierManager.getInstance().freshBank();
//
//                CommonManager.getInstance().freshCity();
////                CashierManager.getInstance().freshCashBalance(FundBrief.Money_Type.CN);
//                CashierManager.getInstance().freshBankCard();
//
//                DiscoverManager.getInstance().freshTraderFocusList(new MResults<List<FocusInfo>>() {
//                    @Override
//                    public void onResult(MResultsInfo<List<FocusInfo>> result) {
//
//                        assertEquals(true, result.isSuccess);
//                    }
//                });
//
//
//                DiscoverManager.getInstance().freshFocusList(new MResults<List<FocusInfo>>() {
//                    @Override
//                    public void onResult(MResultsInfo<List<FocusInfo>> result) {
//                        assertEquals(true, result.isSuccess);
//
//                        TotalInfo totalInfo = DiscoverManager.getInstance().totalInfo;
//
////                        latch.countDown();
//                    }
//                });
//
//                CommonManager.getInstance().freshRedPoint(null, new MResults<Void>() {
//
//                    @Override
//                    public void onResult(MResultsInfo<Void> result) {
//
//                        FortuneManager.getInstance().bountyRedPoint.clear();
//
//                        assertEquals(true, result.isSuccess);
//                    }
//                });
//
//                FortuneManager.getInstance().freshBountyAccount(new MResults<Void>() {
//                    @Override
//                    public void onResult(MResultsInfo<Void> result) {
//
//                        assertEquals(true, result.isSuccess);
//
////                        FortuneManager.getInstance().freshBountyRuleInfos(null);
//
////                        FortuneManager.getInstance().cnBountyAccount.freshBountyList(new MResults<Void>() {
////                            @Override
////                            public void onResult(MResultsInfo<Void> result) {
////
////                                assertEquals(true, result.isSuccess);
////
////                                latch.countDown();
////                            }
////                        });
//                    }
//                });
//
//
//                TraderManager.getInstance().getFundList(true, new MResults<List<FundBrief>>() {
//                    @Override
//                    public void onResult(MResultsInfo<List<FundBrief>> result) {
//
//                        assertEquals(true, result.isSuccess);
//
//                        List<FundBrief> list = result.data;
//
//                        if (list.size() > 0) {
//                            FundBrief sfund = list.get(0);
//
//
//                            TraderManager.getInstance().getPortfolio(false, sfund.index,
//                                    new MResults<Fund>() {
//
//                                        @Override
//                                        public void onResult(MResultsInfo<Fund> result) {
//                                            assertEquals(true, result.isSuccess);
//
//                                        }
//                                    });
//
//                            TraderManager.getInstance().freshTradeInfo(sfund, new MResults<FundTrade>() {
//                                @Override
//                                public void onResult(MResultsInfo<FundTrade> result) {
//
//                                    assertEquals(true, result.isSuccess);
//                                }
//                            });
//
//
//                            TraderManager.getInstance().getInvestors(sfund, new MResults<List<FundInvestor>>() {
//                                @Override
//                                public void onResult(MResultsInfo<List<FundInvestor>> result) {
//
//                                    assertEquals(true, result.isSuccess);
//                                }
//                            });
//                        }
////
////                        Order order = new Order();
////                        order.orderType = Order.Order_Type.Normal.toInt();
////                        order.buy = true;
////                        order.stock = new StockBrief();
////                        order.stock.index = "000001.SH";
////                        order.entrustPrice = 1.0;
////                        order.entrustAmount = 1000;
////                        order.fundID = 11222;
////
////                        List<Order> orders = new ArrayList<Order>();
////                        orders.add(order);
////
////                        StockManager.getInstance().orderStock(order.stock,
////                                orders,
////                                new MResults<Map<Integer, MResultsInfo<Void>>>() {
////                                    @Override
////                                    public void onResult(MResultsInfo<Map<Integer, MResultsInfo<Void>>> result) {
////
////                                        assertEquals(true, result.isSuccess);
////                                        latch.countDown();
////                                    }
////                                });
////
//                    }
//                });
//            }
//        });
//
//        latch.await();
//    }
}
