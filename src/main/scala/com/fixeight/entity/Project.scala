package com.fixeight.entity

import scalikejdbc._

case class Project(
  id: Long,
  name: String,
  owner: Long) {

  def save()(implicit session: DBSession = Project.autoSession): Project = Project.save(this)(session)

  def destroy()(implicit session: DBSession = Project.autoSession): Unit = Project.destroy(this)(session)

}


object Project extends SQLSyntaxSupport[Project] {

  override val tableName = "PROJECTS"

  override val columns = Seq("ID", "NAME", "OWNER")

  def apply(p: SyntaxProvider[Project])(rs: WrappedResultSet): Project = apply(p.resultName)(rs)
  def apply(p: ResultName[Project])(rs: WrappedResultSet): Project = new Project(
    id = rs.get(p.id),
    name = rs.get(p.name),
    owner = rs.get(p.owner)
  )

  val p = Project.syntax("p")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Project] = {
    withSQL {
      select.from(Project as p).where.eq(p.id, id)
    }.map(Project(p.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Project] = {
    withSQL(select.from(Project as p)).map(Project(p.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Project as p)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Project] = {
    withSQL {
      select.from(Project as p).where.append(where)
    }.map(Project(p.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Project] = {
    withSQL {
      select.from(Project as p).where.append(where)
    }.map(Project(p.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Project as p).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    owner: Long)(implicit session: DBSession = autoSession): Project = {
    val generatedKey = withSQL {
      insert.into(Project).columns(
        column.name,
        column.owner
      ).values(
        name,
        owner
      )
    }.updateAndReturnGeneratedKey.apply()

    Project(
      id = generatedKey,
      name = name,
      owner = owner)
  }

  def batchInsert(entities: Seq[Project])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'name -> entity.name,
        'owner -> entity.owner))
        SQL("""insert into PROJECTS(
        NAME,
        OWNER
      ) values (
        {name},
        {owner}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Project)(implicit session: DBSession = autoSession): Project = {
    withSQL {
      update(Project).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.owner -> entity.owner
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Project)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Project).where.eq(column.id, entity.id) }.update.apply()
  }

}
