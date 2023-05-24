DROP PROCEDURE IF EXISTS memberInsert;
DELIMITER
CREATE PROCEDURE memberInsert (
)
BEGIN
    -- 변수 선언
    DECLARE member_id bigint;
    DECLARE product_id bigint;
	DECLARE strlogin VARCHAR(255);
    DECLARE count bigint;
    DECLARE member_max bigint; -- 생성 회원 수
	DECLARE product_per_member_max bigint; -- 회원당 몇개의 상품을 등록할지
    
    set member_max=10;
    set product_per_member_max=100;
    set member_id=1;
    set product_id=1;
    
    
    while member_id <= member_max DO
		set strlogin= cast(member_id as char(255));
		insert into member(member_id,created_date,last_modified_date,birth,email,login_id,name,password,phone_number,status) VALUES(member_id,now(),now(),'990114','sdfwef@kumoh.ac.kr',strlogin,strlogin,strlogin,"010-ssss-ssss",'EXIST'   );
			set count = 0;
			while count < product_per_member_max DO
				insert into auction(auction_id,auction_end_date,auction_start_date,buyer_trans_status,seller_trans_status,start_price,now_price, price_unit,status) VALUES(product_id, DATE_ADD(NOW(), INTERVAL 10 DAY), now(), 'TRANS_BEFORE', 'TRANS_BEFORE' ,10000,10000,1000 ,'BID'  );
				insert into product(product_id,created_date,last_modified_date,info,name,product_status,view_count,auction_id,category_id, member_id) VALUES(product_id,now(), now(), 'info',cast(product_id as char(255)),'EXIST',mod(product_id ,40)+1, product_id, mod(product_id ,18)+1, member_id );
				
                -- 상품 대표 사진 
                insert into file(dtype,created_date,last_modified_date,full_path,original_name,path,type,product_id)
                values('ProductImage',now(),now(),'full path','original name','path', 'SIGNATURE', product_id);
				-- 상품 일반 사진 
                insert into file(dtype,created_date,last_modified_date,full_path,original_name,path,type,product_id)
                values('ProductImage',now(),now(),'full path','original name','path', 'ORDINAL', product_id);
                
			
				set count = count+1;
				set product_id=product_id+1;
			end while;
		set member_id=member_id+1;
    end while;
END
DELIMITER

INSERT INTO `category` VALUES (1,'디지털기기'),(2,'생활가전'),(3,'가구/인테리어'),(4,'생활/주방'),(5,'유아동'),(6,'유아도서'),(7,'여성의류'),(8,'여성잡화'),(9,'도서'),(10,'가공식품'),(11,'반려동물용품'),(12,'식품'),(13,'기타'),(14,'남성패션/잡화'),(15,'뷰티/미용'),(16,'티켓/교환권'),(17,'스포츠/레저'),(18,'취미/게임/음반');
DELIMITER
CALL memberInsert();

