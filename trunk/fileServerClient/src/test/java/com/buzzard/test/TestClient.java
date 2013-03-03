package com.buzzard.test;

import com.buzzard.gui.GuiManager;

import junit.framework.TestCase;



public class TestClient extends TestCase
{
  
  public TestClient(String name)
  {
    super(name);
  }
  
  public void testClient()
  {
    GuiManager.main(null);
  }
  
  
}


