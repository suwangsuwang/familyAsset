package org.swan.familyasset.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.swan.familyasset.Entity.OutboxEvent;

import java.util.List;

@Mapper
public interface OutboxEventMapper {

    @Insert("""
            INSERT INTO outbox_event(
                event_type,
                aggregate_id,
                payload,
                status
               )
               VALUES(
                 #{eventType},
                 #{aggregateId},
                 #{payload},
                 #{status}
               )
            """)
    int insert(OutboxEvent event);

    @Select("""
            SELECT
                id,
                event_type AS eventType,
                aggregate_id AS aggregateId,
                payload,
                status,
                created_at AS createdAt
            FROM outbox_event
            WHERE status = 'NEW'
            ORDER BY id
            """)
    List<OutboxEvent> findNewEvents();

    @Update("""
            UPDATE outbox_event
            SET status = 'SENT'
            WHERE id = #{id}
              AND status = 'NEW'
            """)
    int markSent(Long id);
}
