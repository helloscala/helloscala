package helloscala.common.data

import helloscala.common.jackson.Jackson
import helloscala.test.HelloscalaSpec

class ProvinceTest extends HelloscalaSpec {

  "ProvinceTest" in {
    val str = """万州区 涪陵区 渝中区 大渡口区 江北区 沙坪坝区 九龙坡区 南岸区 北碚区 綦江区 大足区 渝北区 巴南区 黔江区 长寿区 江津区 合川区 永川区 南川区 潼南区 铜梁区 荣昌区 璧山区 梁平县 城口县 丰都县 垫江县 武隆县 忠县 开县 云阳县 奉节县 巫山县 巫溪县 石柱土家族自治县 秀山土家族苗族自治县 酉阳土家族苗族自治县 彭水苗族土家族自治县"""
    val counties = str.split(' ').map(_.trim).map(name => County(name, None))
    counties.foreach(println)
    val jsonStr = Jackson.defaultObjectMapper.writeValueAsString(counties)
    println(jsonStr)
  }

  "ProvinceData" in {
    Province.provinces.provinces.size must be > 0
  }

}
