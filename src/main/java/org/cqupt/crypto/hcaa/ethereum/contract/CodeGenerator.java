package org.cqupt.crypto.hcaa.ethereum.contract;

import org.web3j.codegen.TruffleJsonFunctionWrapperGenerator;
import java.io.File;

public class CodeGenerator {
    public static void main(String[] args) {
        // 1. 定义路径
        String jsonPath = "E:/cross-chain/HCAM/src/main/resources/ethereum/Payment.json";
        String outputPath = "E:/cross-chain/HCAM/src/main/java";
        String packageName = "org.cqupt.crypto.hcaa.ethereum.contract";

        try {
            System.out.println("正在启动生成程序...");

            // 2. 直接构造生成器对象，绕过 main(String[] args) 的参数解析
            // 构造函数参数：(json文件路径, 输出目录, 包名, 是否使用Java原生类型)
            TruffleJsonFunctionWrapperGenerator generator = new TruffleJsonFunctionWrapperGenerator(
                    jsonPath,
                    outputPath,
                    packageName,
                    true // 使用 Java 原生类型（如 BigInteger 而不是 Uint256）
            );

            // 3. 执行生成
            generator.generate();

            System.out.println("-------------------------------------------");
            System.out.println("生成成功！");
            System.out.println("请在左侧目录检查：" + packageName);
            System.out.println("如果没看到文件，请在项目根目录右键点击 'Reload from Disk'");
            System.out.println("-------------------------------------------");

        } catch (Exception e) {
            System.err.println("生成失败，错误信息如下：");
            e.printStackTrace();
        }
    }
}
