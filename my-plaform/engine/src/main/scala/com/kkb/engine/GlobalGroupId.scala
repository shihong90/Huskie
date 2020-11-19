package com.kkb.engine

object GlobalGroupId {
  var groupId:Int =0
  def getGroupId:Int = {
    this.synchronized{
      groupId+=1;
      groupId
    }
  }

}
