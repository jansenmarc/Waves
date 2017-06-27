package com.wavesplatform.it.transactions

import com.wavesplatform.it.{IntegrationSuiteWithThreeAddresses, Node}
import com.wavesplatform.it.util._
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

class TransferTransactionSpecification(override val allNodes: Seq[Node]) extends IntegrationSuiteWithThreeAddresses {
  test("asset transfer changes sender's and recipient's asset balance; issuer's waves balance is decreased by fee") {
    val f = for {
      _ <- assertBalances(firstAddress, 100 waves, 100 waves)
      _ <- assertBalances(secondAddress, 100 waves, 100 waves)

      issuedAssetId <- sender.issue(firstAddress, "name", "description", 100000, 2, reissuable = false, fee = 10 waves).map(_.id)

      _ <- Future.traverse(allNodes)(_.waitForTransaction(issuedAssetId))

      _ <- assertBalances(firstAddress, 90 waves, 90 waves)
      _ <- assertAssetBalance(firstAddress, issuedAssetId, 100000)

      transferTransaction <- sender.transfer(firstAddress, secondAddress, 100000, fee = 10 waves, Some(issuedAssetId)).map(_.id)

      _ <- Future.traverse(allNodes)(_.waitForTransaction(transferTransaction))

      _ <- assertBalances(firstAddress, 80 waves, 80 waves)
      _ <- assertBalances(secondAddress, 100 waves, 100 waves)

      _ <- assertAssetBalance(firstAddress, issuedAssetId, 0)
      _ <- assertAssetBalance(secondAddress, issuedAssetId, 100000)
    } yield succeed

    Await.result(f, 1 minute)
  }

  test("waves transfer changes waves balances and eff.b.") {
    val f = for {
      _ <- assertBalances(firstAddress, 80 waves, 80 waves)
      _ <- assertBalances(secondAddress, 100 waves, 100 waves)

      transferId <- sender.transfer(firstAddress, secondAddress, 5 waves, fee = 5 waves).map(_.id)

      _ <- Future.traverse(allNodes)(_.waitForTransaction(transferId))

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)
    } yield succeed

    Await.result(f, 1 minute)
  }

  test("can not make transfer without having enough of fee") {
    val f = for {
      fb <- Future.traverse(allNodes)(_.height).map(_.min)

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)

      transferFailureAssertion <- assertBadRequest(sender.transfer(secondAddress, firstAddress, 104 waves, fee = 2 waves))

      _ <- Future.traverse(allNodes)(_.waitForHeight(fb + 2))

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)
    } yield transferFailureAssertion

    Await.result(f, 1 minute)
  }



  test("can not make transfer without having enough of waves") {
    val f = for {
      fb <- Future.traverse(allNodes)(_.height).map(_.min)

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)

      transferFailureAssertion <- assertBadRequest(sender.transfer(secondAddress, firstAddress, 106 waves, fee = 1 waves))

      _ <- Future.traverse(allNodes)(_.waitForHeight(fb + 2))

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)
    } yield transferFailureAssertion

    Await.result(f, 1 minute)
  }

  test("can not make transfer without having enough of effective balance") {
    val f = for {
      fb <- Future.traverse(allNodes)(_.height).map(_.min)

      _ <- assertBalances(firstAddress, 70 waves, 70 waves)
      _ <- assertBalances(secondAddress, 105 waves, 105 waves)

      createdLeaseTxId <- sender.lease(firstAddress, secondAddress, 5 waves, fee = 5 waves).map(_.id)

      _ <- Future.traverse(allNodes)(_.waitForTransaction(createdLeaseTxId))

      _ <- assertBalances(firstAddress, 65 waves, 60 waves)
      _ <- assertBalances(secondAddress, 105 waves, 110 waves)

      transferFailureAssertion <- assertBadRequest(sender.transfer(firstAddress, secondAddress, 64 waves, fee = 1 waves))

      _ <- Future.traverse(allNodes)(_.waitForHeight(fb + 2))

      _ <- assertBalances(firstAddress, 65 waves, 60 waves)
      _ <- assertBalances(secondAddress, 105 waves, 110 waves)
    } yield transferFailureAssertion

    Await.result(f, 1 minute)
  }

  test("can not make transfer without having enough of your own waves") {
    val f = for {
      fb <- Future.traverse(allNodes)(_.height).map(_.min)

      _ <- assertBalances(firstAddress, 65 waves, 60 waves)
      _ <- assertBalances(secondAddress, 105 waves, 110 waves)

      createdLeaseTxId <- sender.lease(firstAddress, secondAddress, 5 waves, fee = 5 waves).map(_.id)

      _ <- Future.traverse(allNodes)(_.waitForTransaction(createdLeaseTxId))

      _ <- assertBalances(firstAddress, 60 waves, 50 waves)
      _ <- assertBalances(secondAddress, 105 waves, 115 waves)

      transferFailureAssertion <- assertBadRequest(sender.transfer(secondAddress, firstAddress, 109 waves, fee = 1 waves))

      _ <- Future.traverse(allNodes)(_.waitForHeight(fb + 2))

      _ <- assertBalances(firstAddress, 60 waves, 50 waves)
      _ <- assertBalances(secondAddress, 105 waves, 115 waves)
    } yield transferFailureAssertion

    Await.result(f, 1 minute)
  }
}