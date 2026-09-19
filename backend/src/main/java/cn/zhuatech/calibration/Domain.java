/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.calibration;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.calibration.Model.*;
import static cn.zhuatech.calibration.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("即将到期/逾期器具",e.all(u,"instruments").stream().filter(r->!r.state().equals("RETIRED")&&!date(r.data(),"dueDate").isAfter(LocalDate.now().plusDays(30))).count());out.put("已签发证书",e.all(u,"certificates").size());out.put("隔离器具",e.all(u,"instruments").stream().filter(r->r.state().equals("QUARANTINED")).count());;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "instruments" -> unique(e,u,module,d,"serial");
case "jobs" -> {Row instrument=e.ref(u,d,"instrument","instruments");Row standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED"),"器具已报废");require(t(instrument,"unit").equals(t(standard,"unit")),"标准器与受检器具单位不一致");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
case "readings" -> {Row job=e.ref(u,d,"job","jobs");require(job.state().equals("RUNNING"),"仅进行中的任务允许录入测量点");require(linked(e,u,"readings","job",job.id()).stream().noneMatch(x->t(x,"point").equals(txt(d,"point"))),"测量点编号重复");} default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "instruments.retire" -> require(e.all(u,"jobs").stream().noneMatch(j->t(j,"instrument").equals(r.id())&&!Set.of("APPROVED","CANCELLED").contains(j.state())),"仍有未结校准任务");
case "jobs.start" -> {
 Row standard=e.ref(u,d,"standard","standards");require(!date(d,"performedAt").isAfter(date(standard.data(),"validUntil")),"标准器证书已过期");
 require(e.all(u,"jobs").stream().noneMatch(j->!j.id().equals(r.id())&&t(j,"instrument").equals(txt(d,"instrument"))&&Set.of("RUNNING","REVIEW").contains(j.state())),"器具已有进行中校准任务");
}
case "jobs.evaluate" -> {
 List<Row> points=linked(e,u,"readings","job",r.id());require(points.size()>=2,"至少需要两个测量点");BigDecimal max=BigDecimal.ZERO;
 for(Row point:points){BigDecimal error=n(point,"measured").subtract(n(point,"nominal")).abs();max=max.max(error);var pd=copy(point);pd.put("error",error);pd.put("result",error.compareTo(z(d,"tolerance"))<=0?"PASS":"FAIL");change(e,u,point,"LOCKED",pd,"测量结果锁定");}
 d.put("maxError",max);d.put("result",max.compareTo(z(d,"tolerance"))<=0?"PASS":"FAIL");d.put("pointCount",points.size());
}
case "jobs.reject" -> {for(Row point:linked(e,u,"readings","job",r.id()))change(e,u,point,"DRAFT",copy(point),"复核退回解锁");d.remove("result");d.remove("maxError");}
case "jobs.approve" -> {
 Row standard=e.ref(u,d,"standard","standards");require(!date(d,"performedAt").isAfter(date(standard.data(),"validUntil")),"标准器证书过期");
 Row instrument=e.ref(u,d,"instrument","instruments");require(!instrument.state().equals("RETIRED"),"器具已报废");
 LocalDate due=date(d,"performedAt").plusDays(n(instrument,"cycleDays").longValueExact());var id=copy(instrument);
 if(txt(d,"result").equals("PASS"))id.put("dueDate",due.toString());id.put("lastCalibration",date(d,"performedAt").toString());id.put("lastResult",txt(d,"result"));
 change(e,u,instrument,txt(d,"result").equals("PASS")?"ACTIVE":"QUARANTINED",id,"校准复核更新器具");
 e.ledger(u,"certificates","ISSUED",Map.of("job",r.id(),"instrument",instrument.id(),"standardCertificate",t(standard,"certificate"),"result",txt(d,"result"),"validUntil",due.toString(),"maxError",z(d,"maxError"),"reviewer",u.username(),"points",linked(e,u,"readings","job",r.id()).stream().map(Row::data).toList()));
}
case "jobs.cancel" -> {d.putAll(i);for(Row point:linked(e,u,"readings","job",r.id()))change(e,u,point,"VOID",copy(point),"校准撤销，保留测量记录");}
 default -> {} }return null;
 }
}
