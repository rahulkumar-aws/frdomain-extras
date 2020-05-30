package frdomain.ch6.domain
package repository

import java.util.Date
import model.{ Account, Balance }
import common._
import zio._

class InMemoryAccountRepository(ref: Ref[Map[String, Account]])  {
  val accountRepository = new AccountRepository.Service {

    override def all: UIO[Seq[Account]] =
      ref.get.map(_.values.toList)

    override def query(no: String): UIO[Option[Account]] =
      ref.get.map(_.get(no))

    override def query(openedOn: Date): UIO[Seq[Account]] =
      ref.get.map(_.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toSeq)

    override def store(a: Account): UIO[Account] =
      ref.update(m => m + (a.no -> a)).map(_ => a)

    override def balance(no: String): UIO[Balance] =
      ref.get.map(_.get(no).map(_.balance).getOrElse(Balance()))
  }
}

object InMemoryAccountRepository {

  val layer: ZLayer[Any, Nothing, AccountRepository] =
    ZLayer.fromEffect {
      for {
        ref <- Ref.make(Map.empty[String, Account])
      } yield new InMemoryAccountRepository(ref).accountRepository
    }
}