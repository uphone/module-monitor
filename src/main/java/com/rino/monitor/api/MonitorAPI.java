package com.rino.monitor.api;

import cn.hutool.system.oshi.OshiUtil;
import com.rino.monitor.bean.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author zip */
@RestController
@Slf4j
public class MonitorAPI {
  @Value("${module.monitor.main}")
  private String mainFs;

  /** 系统信息 */
  @GetMapping("/os")
  public ApiResult osInfo() {
    OshiUtil.getOs().toString();
    String data = OshiUtil.getOs().toString();
    return new ApiResult(data);
  }

  /** 内存信息(总量,已使用),单位:byte */
  @GetMapping("/mem")
  public ApiResult memInfo() {
    GlobalMemory memory = OshiUtil.getMemory();
    long total = memory.getTotal();
    long free = memory.getAvailable();
    Map data = new HashMap(2);
    data.put("total", total);
    data.put("used", total - free);
    return new ApiResult(data);
  }

  /** 文件系统(总量,已使用),单位:byte */
  @GetMapping("/fs")
  public ApiResult fsInfo() {
    FileSystem fileSystem = OshiUtil.getOs().getFileSystem();
    List<OSFileStore> fsArray = fileSystem.getFileStores();
    long total = 0, free = 0;
    for (OSFileStore fs : fsArray) {
      log.info(
          "name:{},volume:{}, mount:{}, total:{}, free:{}",
          fs.getName(),
          fs.getVolume(),
          fs.getMount(),
          fs.getTotalSpace(),
          fs.getFreeSpace());
      String options = fs.getOptions();
      boolean mainFlag = mainFs.equals(fs.getVolume());
      //    "/".equals(fs.getVolume()) || "rootfs".equalsIgnoreCase(fs.getName()) || (options !=
      // null && options.indexOf("rootfs") != -1);
      if (mainFlag) {
        total += fs.getTotalSpace();
        free += fs.getFreeSpace();
      }
    }
    Map data = new HashMap(2);
    data.put("total", total);
    data.put("used", total - free);
    return new ApiResult(data);
  }
}
