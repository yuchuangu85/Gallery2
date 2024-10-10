# Gellery2

> base android15-s1-release

## 编译说明
1.原生代码编译不支持Gradle编译，这里进行了修改，支持Android studio编译。

2.jni_libjpeg_turbo代码来源
* 原生方案
  * http://www.ijg.org/files/ 
  * http://libjpeg.sourceforge.net/

* 替代方案
编译jni_jpegstream时需要依赖libjpeg.a和libturbojpeg.a两个静态库，需要使用下面库根据对应教程编译生成，当前已经编译放到了jniLibs里面
  * https://github.com/libjpeg-turbo/libjpeg-turbo
  * https://blog.csdn.net/unonoi/article/details/121386689 -- 编译


3.jniLibs-backup为备用

## 流程图和数据结构图

由于项目代码还在分析中，所以还没有绘制完成，所以之后会不断更新 ------ 2018-04-29 更新

<img src="/img/Gallery_ui1.jpg"/> 
<img src="/img/Gallery_data.jpg"/> 
<img src="/img/DataManager.jpg"/> 
<img src="/img/GalleryStart.jpg"/>