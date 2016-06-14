Feature: test

  Scenario Outline: Opening <scan> Scan Settings
    Given I am on Antivirus page
    When  I go to other scans section
    And Open <scan> Scan settings
    Then I can see <scan> Scan settings window
    Examples:
      | scan            |
      | Quick           |
      | Boot time       |
      | Removable media |
      | Folder Scan     |