public class Main extends java.lang.Object
{
   public static void main(java.lang.String[] _args)
   {
      java.lang.System.out.println("测试开始");
      // 文件分割
      java.lang.String[] spilt = SpiltMerge.spilt(
         "/storage/emulated/0/.dlprovider/vmos.apk",
         "/storage/emulated/0/.dlprovider/spilt/",
         5 * 1024 * 1024);
      java.lang.System.out.println("文件分割完成 " + java.util.Arrays.deepToString(spilt));
      // 文件合并
      java.lang.String merge = SpiltMerge.merge(spilt,"/storage/emulated/0/.dlprovider/merge/");
      java.lang.System.out.println("文件合并完成 " + merge);
      java.lang.System.out.println("测试结束");
   }
}

class SpiltMerge extends java.lang.Object
{
   // 获取路径前缀名
   public static java.lang.String getPrefixName(java.lang.String _path)
   {
      // /storage/emulated0/.android/.log.txt
      if (_path == null)
      {
         return "";
      }
      java.io.File file = new java.io.File(_path);
      java.lang.String name = file.getName();
      if (name.lastIndexOf(".") != -1)
      {
         return name.substring(0, name.lastIndexOf("."));
      }
      return name.substring(0, name.length());
   }
   
   // 获取路径后缀名
   public static java.lang.String getSuffixName(java.lang.String _path)
   {
      // /storage/emulated0/.android/.log.txt
      if (_path == null)
      {
         return "";
      }
      java.io.File file = new java.io.File(_path);
      java.lang.String name = file.getName();
      if (name.lastIndexOf(".") != -1)
      {
         return name.substring(name.lastIndexOf(".") + 1, name.length());
      }
      return "";
   }
   
   /**
    * 文件分割
    * @param java.lang.String _inPath 待分割文件路径
    * @param java.lang.String _outPath 文件输出路径
    * @param long _spiltSize 文件分割大小
    * @return java.lang.String[] 分割后文件名路径数组
    */
   public static java.lang.String[] spilt(java.lang.String _inPath, java.lang.String _outPath, long _spiltSize)
   {
      java.lang.String uuid = java.util.UUID.randomUUID().toString();
      java.io.File infile = new java.io.File(_inPath);
      // 判断输入文件是否有效 分割大小是否大于等于文件大小 大小是否为零
      if (!infile.isFile() || (_spiltSize >= infile.length()) || (_spiltSize == 0) || (infile.length() == 0))
      {
         if (_spiltSize >= infile.length())
         {
            java.lang.System.out.println("Notice: [spilt()] 分割大小 " + _spiltSize + " >= 文件大小 " + infile.length() + " ，无需分割并终止操作");
         }
         return null;
      }
      java.io.File outfile = new java.io.File(_outPath);
      // 判断输出路径目录是否存在 是不是文件
      if (!outfile.exists())
      {
         if (outfile.isFile())
         {
            return null;
         }
         outfile.mkdirs();
      }
      java.lang.System.out.println("Notice: [spilt()] 输入文件 = " + infile.getAbsolutePath() + ", 输出文件 = " + outfile.getAbsolutePath());
      int count;
      // 若能整除
      if ((infile.length() % _spiltSize) == 0)
      {
         count = (int)(infile.length() / _spiltSize);
      }
      else
      {
         count = ((int)(infile.length() / _spiltSize)) + 1;
      }
      java.lang.String[] fileNames = new java.lang.String[count];
      java.io.RandomAccessFile raf = null;
      try
      {
         raf = new java.io.RandomAccessFile(infile, "r");
         long length = raf.length();
         long offset = 0L;
         for (int i = 0; i < count; i++)
         {
            long begin = offset;
            long end = (i + 1) * _spiltSize;
            fileNames[i] = new java.io.File(outfile.getAbsolutePath() + java.io.File.separator + infile.getName() + "." + i).getAbsolutePath();
            if (new java.io.File(fileNames[i]).exists())
            {
               if(getSuffixName(infile.getAbsolutePath()) != "")
               {
                  fileNames[i] = new java.io.File(outfile.getAbsolutePath() + java.io.File.separator + uuid + "." + getSuffixName(infile.getAbsolutePath()) + "." + i).getAbsolutePath();
               }
               else
               {
                  fileNames[i] = new java.io.File(outfile.getAbsolutePath() + java.io.File.separator + uuid + "." + i).getAbsolutePath();
               }
            }
            offset = spiltWrite(infile.getAbsolutePath(), fileNames[i], begin, end);
         }
         if ((length - offset) > 0)
         {
            java.lang.System.out.println("Notice: [spilt()] 文件 " + fileNames[count-1] + "数据可能不完整 –> (length - offset) > 0");
            offset = spiltWrite(infile.getAbsolutePath(), fileNames[count-1], offset, length);
         }
         java.lang.System.out.println("Notice: [spilt()] 分割文件总数 = " + count + " –> " + java.util.Arrays.deepToString(fileNames));
      }
      catch (java.lang.Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            raf.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
      }
      return fileNames;
   }
   
   // 文件分割单独写操作
   public static long spiltWrite(java.lang.String _inPath, java.lang.String _outPath, long _begin, long _end)
   {
      java.io.RandomAccessFile in = null;
      java.io.RandomAccessFile out = null;
      long endPointer = 0L;
      try
      {
         java.io.File infile = new java.io.File(_inPath);
         java.io.File outfile = new java.io.File(_outPath);
         if (!outfile.exists())
         {
            in = new java.io.RandomAccessFile(infile, "r");
            out = new java.io.RandomAccessFile(outfile, "rw");
            // 每次读取文件字节数组
            byte[] b = new byte[1024];
            int n = 0;
            // 从指定位置读取文件字节流
            in.seek(_begin);
            // 判断文件流读取边界
            while ((in.getFilePointer() <= _end) && ((n = in.read(b)) != -1))
            {
               // 从指定每一份文件范围写入不同文件
               out.write(b, 0, n);
            }
            // 当前读取文件指针
            endPointer = in.getFilePointer();
         }
         else
         {
            if (outfile.delete())
            {
               java.lang.System.out.println("Notice: [spiltWrite()] 已删除存在文件 –> " + outfile.getAbsolutePath());
               return spiltWrite(_inPath, _outPath, _begin, _end);
            }
         }
         java.lang.System.out.println("Notice: [spiltWrite()] 输入文件 = " + infile.getAbsolutePath() + ", 输出文件 = " + outfile.getAbsolutePath());
      }
      catch (java.lang.Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            in.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
         try
         {
            out.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
      }
      return endPointer;
   }
   
   /**
    * 文件合并
    * @param java.lang.String[] _fileNames 分割后文件名路径数组
    * @param java.lang.String _outPath 文件输出路径
    * @return java.lang.String 合并后文件路径名
    */
   public static java.lang.String merge(java.lang.String[] _fileNames, java.lang.String _outPath)
   {
      java.lang.String uuid = java.util.UUID.randomUUID().toString();
      if ((_fileNames == null) || (_fileNames.length <= 0))
      {
         return null;
      }
      // 判断输出路径是否不存在 输出路径是否是文件
      if (!new java.io.File(_outPath).exists())
      {
         new java.io.File(_outPath).mkdirs();
      }
      else if (new java.io.File(_outPath).isFile())
      {
         java.lang.System.out.println("Notice: [merge()] 输出路径是个文件，操作被终止 –> " + new java.io.File(_outPath).getAbsolutePath());
         return null;
      }
      // 临时文件
      java.io.File infile = new java.io.File(_outPath + java.io.File.separator + uuid + ".tmp");
      // 输出文件路径名
      java.lang.String filename = infile.getAbsolutePath();
      java.io.RandomAccessFile out = null;
      try
      {
         out = new java.io.RandomAccessFile(infile, "rw");
         for (int i = 0; i < _fileNames.length; i++)
         {
            java.io.RandomAccessFile in = null;
            try
            {
               in = new java.io.RandomAccessFile(new java.io.File(_fileNames[i]), "r");
               byte[] b = new byte[1024];
               int n = 0;
               while ((n = in.read(b)) != -1)
               {
                  out.write(b, 0, n);
               }
            }
            catch (java.lang.Throwable e)
            {
               e.printStackTrace();
            }
            finally
            {
               try
               {
                  in.close();
               }
               catch (java.lang.Throwable e)
               {
                  e.printStackTrace();
               }
            }
         }
      }
      catch (java.lang.Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            out.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
      }
      // 若旧文件前缀名不为空且新文件名不存在则改变输出文件路径
      if (getPrefixName(_fileNames[0]) != "")
      {
         java.io.File outfile = new java.io.File(_outPath + java.io.File.separator + getPrefixName(_fileNames[0]));
         if (!outfile.exists())
         {
            if (infile.renameTo(outfile))
            {
               filename = outfile.getAbsolutePath();
            }
         }
         // 否则若新文件后缀名不为空则改变输出文件后缀名和路径
         else if(getSuffixName(outfile.getAbsolutePath()) != "")
         {
            java.io.File newfile = new java.io.File(infile.getParent() + java.io.File.separator + getPrefixName(infile.getAbsolutePath()) + "." + getSuffixName(outfile.getAbsolutePath()));
            if (infile.renameTo(newfile))
            {
               filename = newfile.getAbsolutePath();
            }
         }
      }
      return filename;
   }
   
   /**
    * 文件MD5加密处理
    * @param java.io.File _file 指定合并文件
    * @return java.lang.String
    */
   public static java.lang.String getFileMD5(java.io.File _file)
   {
      if (!_file.isFile())
      {
         return null;
      }
      java.security.MessageDigest digest = null;
      java.io.FileInputStream in = null;
      byte buffer[] = new byte[1024];
      int len;
      try
      {
         digest = java.security.MessageDigest.getInstance("MD5");
         in = new java.io.FileInputStream(_file);
         while ((len = in.read(buffer, 0, 1024)) != -1)
         {
            digest.update(buffer, 0, len);
         }
      }
      catch (java.lang.Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            in.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
      }
      return bytesToHexString(digest.digest());
   }

   public static java.lang.String bytesToHexString(byte[] _src)
   {
      java.lang.StringBuilder sb = new java.lang.StringBuilder("");
      if ((_src == null) || (_src.length <= 0))
      {
         return null;
      }
      for (int i = 0; i < _src.length; i++)
      {
         int v = _src[i] & 0xFF;
         java.lang.String hv = java.lang.Integer.toHexString(v);
         if (hv.length() < 2)
         {
            sb.append(0);
         }
         sb.append(hv);
      }
      return sb.toString();
   }
   
   /**
    * 对文件Base64加密处理
    * @param java.io.File _file 指定加密处理文件
    * @return java.lang.String
    */
   public static java.lang.String getBase64(java.io.File _file)
   {
      java.lang.String filePath = _file.getAbsolutePath();
      java.io.InputStream in = null;
      java.io.ByteArrayOutputStream bos = null;
      byte[] buffer = null;
      try
      {
         in = new java.io.FileInputStream(filePath);
         bos = new java.io.ByteArrayOutputStream();
         byte[] b = new byte[1024];
         int n;
         while ((n = in.read(b)) != -1)
         {
            bos.write(b, 0, n);
         }
         buffer = bos.toByteArray();
      }
      catch (java.lang.Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            in.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
         try
         {
            bos.close();
         }
         catch (java.lang.Throwable e)
         {
            e.printStackTrace();
         }
      }
      return encodeByte(buffer);
   }
   
   /**
    * Base64编码处理
    * @param byte[] _buffer 指定编码处理字段
    * @return java.lang.String 编码后
    */
   public static java.lang.String encodeByte(byte[] _buffer)
   {
      // return android.util.Base64.encodeToString(_buffer, android.util.Base64.DEFAULT);
      return java.util.Base64.getEncoder().encodeToString(_buffer);
   }
   
   /**
    * Base64解码处理
    * @param java.lang.String _base64Token 指定编码字段处理
    * @return java.lang.String 解码后
    */
   public static byte[] docodeByte(java.lang.String _base64Token)
   {
      // return android.util.Base64.decode(_base64Token, android.util.Base64.DEFAULT);
      return java.util.Base64.getDecoder().decode(_base64Token);
   }
}
