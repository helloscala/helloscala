/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.http.session

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait RefreshTokenStorage[T] {
  def lookup(selector: String): Future[Option[RefreshTokenLookupResult[T]]]

  def store(data: RefreshTokenData[T]): Future[Unit]

  def remove(selector: String): Future[Unit]

  def schedule[S](after: Duration)(op: => Future[S]): Unit
}

case class RefreshTokenData[T](
    forSession: T,
    selector: String,
    tokenHash: String,

    // Timestamp
    expires: Long)

case class RefreshTokenLookupResult[T](
    tokenHash: String,

    // Timestamp
    expires: Long,
    createSession: () => T)

/**
 * Useful for testing.
 */
trait InMemoryRefreshTokenStorage[T] extends RefreshTokenStorage[T] {

  case class Store(session: T, tokenHash: String, expires: Long)

  private val _store = mutable.Map[String, Store]()

  def store: Map[String, Store] = _store.toMap

  override def lookup(selector: String) = {
    Future.successful {
      val r = _store.get(selector).map(s => RefreshTokenLookupResult[T](s.tokenHash, s.expires,
        () => s.session))
      log(s"Looking up token for selector: $selector, found: ${r.isDefined}")
      r
    }
  }

  override def store(data: RefreshTokenData[T]) = {
    log(s"Storing token for selector: ${data.selector}, user: ${data.forSession}, " +
      s"expires: ${data.expires}, now: ${System.currentTimeMillis()}")
    Future.successful(_store.put(data.selector, Store(data.forSession, data.tokenHash, data.expires)))
  }

  override def remove(selector: String) = {
    log(s"Removing token for selector: $selector")
    Future.successful(_store.remove(selector))
  }

  override def schedule[S](after: Duration)(op: => Future[S]) = {
    log("Running scheduled operation immediately")
    op
    Future.successful(())
  }

  def log(msg: String): Unit
}
