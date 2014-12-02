import cymysql

conn = cymysql.connect(
    host='127.0.0.1', 
    user='root', 
    passwd='password', 
    db='flights', 
    charset='utf8'
    )

cur = conn.cursor()
cur.execute('select * from flights')

for r in cur.fetchall():
    print(r);
