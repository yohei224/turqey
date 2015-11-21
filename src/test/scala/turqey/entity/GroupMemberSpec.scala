package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class GroupMemberSpec extends Specification {

  "GroupMember" should {

    val gm = GroupMember.syntax("gm")

    "find by primary keys" in new AutoRollback {
      val maybeFound = GroupMember.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = GroupMember.findBy(sqls.eq(gm.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = GroupMember.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = GroupMember.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = GroupMember.findAllBy(sqls.eq(gm.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = GroupMember.countBy(sqls.eq(gm.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = GroupMember.create(groupId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = GroupMember.findAll().head
      // TODO modify something
      val modified = entity
      val updated = GroupMember.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = GroupMember.findAll().head
      GroupMember.destroy(entity)
      val shouldBeNone = GroupMember.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = GroupMember.findAll()
      entities.foreach(e => GroupMember.destroy(e))
      val batchInserted = GroupMember.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
