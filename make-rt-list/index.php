<?php

class Processor
{
  private $v7 = "/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/lib/rt.jar";
  private $v8 = "/Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/jre/lib/rt.jar";
  private $v9 = "/Library/Java/JavaVirtualMachines/jdk-9.0.4.jdk/Contents/Home/lib/modules";
  private $v10 = "/Library/Java/JavaVirtualMachines/jdk-10.jdk/Contents/Home/lib/modules";


  function resolvePath()
  {
    $v7 = getenv("JAVA_HOME_V7");
    if ($v7 !== false) {
      $this->v7 = $v7;
    }
    if (!file_exists($this->v7)) die("missing path of v7");

    $v8 = getenv("JAVA_HOME_V8");
    if ($v8 !== false) {
      $this->v8 = $v8;
    }
    if (!file_exists($this->v8)) die("missing path of v8");

    $v9 = getenv("JAVA_HOME_V9");
    if ($v9 !== false) {
      $this->v9 = $v9;
    }
    if (!file_exists($this->v9)) die("missing path of v9");

    $v10 = getenv("JAVA_HOME_V10");
    if ($v10 !== false) {
      $this->v10 = $v10;
    }
    if (!file_exists($this->v10)) die("missing path of v10");
  }

  public function process()
  {
    $this->resolvePath();
    $v7 = $this->makeList78($this->v7);
    $v8 = $this->makeList78($this->v8);
    $v9 = $this->makeList910($this->v9);
    $v10 = $this->makeList910($this->v10);

    $this->write($v7, "7");
    $this->write($v8, "8");
    $this->write($v9, "9");
    $this->write($v10, "10");
    echo "done" . PHP_EOL;
  }

  function makeList78($target)
  {
    $dist = $this->extractZip($target);
    $res = [];
    $flags = FilesystemIterator::KEY_AS_PATHNAME | FilesystemIterator::CURRENT_AS_FILEINFO | FilesystemIterator::SKIP_DOTS;
    $dir = new RecursiveDirectoryIterator($dist, $flags);
    $it = new RecursiveIteratorIterator($dir);
    foreach ($it as $name => $object) {
      if (!$this->endsWith($name, ".class")) continue;

      $str = trim(str_replace($dist, "", $name), "/");
      $str = preg_replace('/\.class$/', '', $str);
      $res[] = $str;
    }
    return $res;
  }

  function makeList910($target)
  {
    if (!$this->isCmdExists("jimage")) die("jimage does not exist");

    $dist = $this->tmpDir();
    $cmd = "jimage extract --dir=$dist $target";
    $code = 0;
    $out = [];
    exec($cmd, $out, $code);
    if ($code === 0) {
      echo "extracted modules: $target -> $dist" . PHP_EOL;
    } else {
      die("unable to extract modules: $target, error: " . implode($out, PHP_EOL));
    }

    $flags = FilesystemIterator::KEY_AS_PATHNAME | FilesystemIterator::CURRENT_AS_FILEINFO | FilesystemIterator::SKIP_DOTS;
    $rt = new RecursiveDirectoryIterator($dist, $flags);
    $res = [];
    /* @var $object SplFileInfo */
    foreach ($rt as $name => $object) {
      if (!$object->isDir()) continue;

      $dir = new RecursiveDirectoryIterator($name, $flags);
      $it = new RecursiveIteratorIterator($dir);
      foreach ($it as $k => $v) {
        if (!$this->endsWith($k, ".class")) continue;

        $n = str_replace("/", "\/", preg_quote($name));
        $str = preg_replace("/^$n/", "", $k);
        $str = trim(str_replace($dist, "", $str), "/");
        $str = preg_replace('/\.class$/', '', $str);
        $res[] = $str;
      }
    }

    return $res;
  }

  function endsWith($haystack, $needle)
  {
    $p = strlen($haystack) - strlen($needle);
    return strrpos($haystack, $needle, 0) === $p;
  }

  function isCmdExists($cmd)
  {
    $code = 0;
    $out = [];
    exec("$cmd -h", $out, $code);
    return $code === 0;
  }

  function tmpDir()
  {
    return implode([sys_get_temp_dir(), uniqid("rt-list-")], DIRECTORY_SEPARATOR);
  }

  function extractZip($target)
  {
    $zip = new ZipArchive();
    if ($zip->open($target) === true) {
      $dist = $this->tmpDir();
      if (!mkdir($dist)) return die("unable to mkdir: $dist");
      $zip->extractTo($dist);
      $zip->close();
      echo "extracted zip: $target -> $dist" . PHP_EOL;
      return $dist;
    }
    return die("unable to unzip: $target");
  }

  function write($list, $v)
  {
    file_put_contents(__DIR__ . "/../src/main/resources/rt-list/$v.txt", implode($list, PHP_EOL));
  }
}

$p = new Processor();
$p->process();