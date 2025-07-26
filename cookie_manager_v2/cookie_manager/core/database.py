"""数据库管理模块，使用SQLAlchemy 2.0"""

import json
from datetime import datetime, timedelta
from typing import List, Optional, Dict, Any

from sqlalchemy import (
    create_engine, text, String, Integer, DateTime, Text, 
    JSON, Enum as SQLEnum, Index
)
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import (
    DeclarativeBase, Mapped, mapped_column, sessionmaker
)
from sqlalchemy.dialects.sqlite import insert

from ..models.cookie import CookieData, CookieStatus
from ..models.config import Settings


class Base(DeclarativeBase):
    """SQLAlchemy基础类"""
    pass


class CookieRecord(Base):
    """Cookie数据库记录"""
    __tablename__ = "cookies"
    
    # 主键
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    
    # 网站信息
    website_name: Mapped[str] = mapped_column(String(100), nullable=False)
    website_url: Mapped[str] = mapped_column(String(500), nullable=False)
    
    # Cookie数据 
    cookies_json: Mapped[str] = mapped_column(Text, nullable=False)
    headers_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    user_agent: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    
    # 状态信息
    status: Mapped[CookieStatus] = mapped_column(
        SQLEnum(CookieStatus), 
        default=CookieStatus.VALID,
        nullable=False
    )
    last_validated: Mapped[datetime] = mapped_column(
        DateTime, 
        default=datetime.now,
        nullable=False
    )
    last_refreshed: Mapped[Optional[datetime]] = mapped_column(
        DateTime, 
        nullable=True
    )
    
    # 时间戳
    created_at: Mapped[datetime] = mapped_column(
        DateTime, 
        default=datetime.now,
        nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, 
        default=datetime.now,
        onupdate=datetime.now,
        nullable=False
    )
    
    # 统计信息
    validation_count: Mapped[int] = mapped_column(Integer, default=0)
    refresh_count: Mapped[int] = mapped_column(Integer, default=0)
    failure_count: Mapped[int] = mapped_column(Integer, default=0)
    
    # 元数据
    metadata_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    
    # 索引
    __table_args__ = (
        Index('idx_website_name', 'website_name'),
        Index('idx_website_url', 'website_url'),
        Index('idx_status', 'status'),
        Index('idx_last_validated', 'last_validated'),
    )
    
    def to_cookie_data(self) -> CookieData:
        """转换为CookieData模型"""
        return CookieData(
            id=self.id,
            website_name=self.website_name,
            website_url=self.website_url,
            cookies=json.loads(self.cookies_json),
            headers=json.loads(self.headers_json) if self.headers_json else None,
            user_agent=self.user_agent,
            status=self.status,
            last_validated=self.last_validated,
            last_refreshed=self.last_refreshed,
            created_at=self.created_at,
            updated_at=self.updated_at,
            validation_count=self.validation_count,
            refresh_count=self.refresh_count,
            failure_count=self.failure_count,
            metadata=json.loads(self.metadata_json) if self.metadata_json else None,
        )
    
    @classmethod
    def from_cookie_data(cls, cookie_data: CookieData) -> "CookieRecord":
        """从CookieData创建记录"""
        return cls(
            id=cookie_data.id,
            website_name=cookie_data.website_name,
            website_url=cookie_data.website_url,
            cookies_json=json.dumps(cookie_data.cookies, ensure_ascii=False),
            headers_json=json.dumps(cookie_data.headers, ensure_ascii=False) if cookie_data.headers else None,
            user_agent=cookie_data.user_agent,
            status=cookie_data.status,
            last_validated=cookie_data.last_validated,
            last_refreshed=cookie_data.last_refreshed,
            created_at=cookie_data.created_at,
            updated_at=cookie_data.updated_at,
            validation_count=cookie_data.validation_count,
            refresh_count=cookie_data.refresh_count,
            failure_count=cookie_data.failure_count,
            metadata_json=json.dumps(cookie_data.metadata, ensure_ascii=False) if cookie_data.metadata else None,
        )


class DatabaseManager:
    """数据库管理器"""
    
    def __init__(self, settings: Settings):
        self.settings = settings
        self.engine = create_engine(
            settings.database.url,
            echo=settings.database.echo,
            pool_size=settings.database.pool_size,
        )
        self.session_factory = sessionmaker(bind=self.engine)
        
    async def init_database(self) -> None:
        """初始化数据库"""
        # 创建所有表
        Base.metadata.create_all(self.engine)
        
        # 运行数据库迁移（如果需要）
        await self._run_migrations()
    
    async def _run_migrations(self) -> None:
        """运行数据库迁移"""
        # 这里可以添加数据库迁移逻辑
        # 例如添加新字段、索引等
        pass
    
    def save_cookie(self, cookie_data: CookieData) -> CookieData:
        """保存Cookie数据"""
        with self.session_factory() as session:
            if cookie_data.id:
                # 更新现有记录
                record = session.get(CookieRecord, cookie_data.id)
                if record:
                    # 更新字段
                    record.cookies_json = json.dumps(cookie_data.cookies, ensure_ascii=False)
                    record.headers_json = json.dumps(cookie_data.headers, ensure_ascii=False) if cookie_data.headers else None
                    record.user_agent = cookie_data.user_agent
                    record.status = cookie_data.status
                    record.last_validated = cookie_data.last_validated
                    record.last_refreshed = cookie_data.last_refreshed
                    record.updated_at = datetime.now()
                    record.validation_count = cookie_data.validation_count
                    record.refresh_count = cookie_data.refresh_count
                    record.failure_count = cookie_data.failure_count
                    record.metadata_json = json.dumps(cookie_data.metadata, ensure_ascii=False) if cookie_data.metadata else None
                else:
                    # 记录不存在，创建新记录
                    record = CookieRecord.from_cookie_data(cookie_data)
                    session.add(record)
            else:
                # 使用upsert操作
                stmt = insert(CookieRecord).values(
                    website_name=cookie_data.website_name,
                    website_url=cookie_data.website_url,
                    cookies_json=json.dumps(cookie_data.cookies, ensure_ascii=False),
                    headers_json=json.dumps(cookie_data.headers, ensure_ascii=False) if cookie_data.headers else None,
                    user_agent=cookie_data.user_agent,
                    status=cookie_data.status,
                    last_validated=cookie_data.last_validated,
                    last_refreshed=cookie_data.last_refreshed,
                    validation_count=cookie_data.validation_count,
                    refresh_count=cookie_data.refresh_count,
                    failure_count=cookie_data.failure_count,
                    metadata_json=json.dumps(cookie_data.metadata, ensure_ascii=False) if cookie_data.metadata else None,
                )
                
                # SQLite特定的ON CONFLICT处理
                stmt = stmt.on_conflict_do_update(
                    index_elements=['website_name', 'website_url'],
                    set_=dict(
                        cookies_json=stmt.excluded.cookies_json,
                        headers_json=stmt.excluded.headers_json,
                        user_agent=stmt.excluded.user_agent,
                        status=stmt.excluded.status,
                        last_validated=stmt.excluded.last_validated,
                        last_refreshed=stmt.excluded.last_refreshed,
                        updated_at=datetime.now(),
                        validation_count=stmt.excluded.validation_count,
                        refresh_count=stmt.excluded.refresh_count,
                        failure_count=stmt.excluded.failure_count,
                        metadata_json=stmt.excluded.metadata_json,
                    )
                )
                
                result = session.execute(stmt)
                record = session.get(CookieRecord, result.lastrowid)
            
            session.commit()
            session.refresh(record)
            return record.to_cookie_data()
    
    def get_cookie(self, website_name: str) -> Optional[CookieData]:
        """获取指定网站的Cookie"""
        with self.session_factory() as session:
            record = session.query(CookieRecord).filter(
                CookieRecord.website_name == website_name
            ).first()
            
            return record.to_cookie_data() if record else None
    
    def get_all_cookies(self) -> List[CookieData]:
        """获取所有Cookie"""
        with self.session_factory() as session:
            records = session.query(CookieRecord).all()
            return [record.to_cookie_data() for record in records]
    
    def get_cookies_by_status(self, status: CookieStatus) -> List[CookieData]:
        """根据状态获取Cookie"""
        with self.session_factory() as session:
            records = session.query(CookieRecord).filter(
                CookieRecord.status == status
            ).all()
            return [record.to_cookie_data() for record in records]
    
    def delete_cookie(self, website_name: str) -> bool:
        """删除指定网站的Cookie"""
        with self.session_factory() as session:
            result = session.query(CookieRecord).filter(
                CookieRecord.website_name == website_name
            ).delete()
            session.commit()
            return result > 0
    
    def cleanup_expired_cookies(self, days: int = 30) -> int:
        """清理过期的Cookie"""
        cutoff_date = datetime.now() - timedelta(days=days)
        
        with self.session_factory() as session:
            result = session.query(CookieRecord).filter(
                CookieRecord.status == CookieStatus.EXPIRED,
                CookieRecord.updated_at < cutoff_date
            ).delete()
            session.commit()
            return result
    
    def get_statistics(self) -> Dict[str, Any]:
        """获取统计信息"""
        with self.session_factory() as session:
            total_count = session.query(CookieRecord).count()
            
            status_counts = {}
            for status in CookieStatus:
                count = session.query(CookieRecord).filter(
                    CookieRecord.status == status
                ).count()
                status_counts[status.value] = count
            
            return {
                "total_cookies": total_count,
                "status_distribution": status_counts,
                "last_updated": datetime.now().isoformat()
            }