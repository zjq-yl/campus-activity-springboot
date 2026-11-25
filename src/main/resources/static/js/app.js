const $=selector=>document.querySelector(selector);
const esc=value=>String(value??'').replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
const date=value=>value?String(value).replace('T',' ').slice(0,16):'—';
let me;
async function api(path,body){const options=body===undefined?{}:{method:'POST',body:new URLSearchParams(body),headers:{'X-CSRF-Token':me?.csrf??''}};const response=await fetch(path,options);const raw=await response.text();let data;try{data=JSON.parse(raw)}catch{data=raw}if(!response.ok)throw Error(data?.error||`请求失败 ${response.status}`);return data}
function message(value,type='success'){const box=$('#message');box.textContent=value;box.className=`alert alert-${type} mb-4`;box.hidden=false;box.scrollIntoView({behavior:'smooth',block:'nearest'})}
async function run(path,body={}){try{const result=await api(path,body);message(result?.code?`签到码：${result.code}`:'操作成功');return result||{ok:true}}catch(error){message(error.message,'danger');return null}}
async function init(role){try{me=await api('/me');if(me.role!==role){location.href='/';return false}$('#currentUser').textContent=me.name;$('#logout').onclick=async()=>{await api('/logout',{});location.href='/'};document.querySelectorAll('.sidebar nav a').forEach(link=>link.classList.toggle('active',link.pathname===location.pathname));return true}catch{location.href='/';return false}}
const pointLabels=['思想成长','实践实习','志愿公益','创新创业','文体活动','社会工作','技能特长'];
