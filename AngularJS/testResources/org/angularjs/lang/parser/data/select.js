sender, select, female {Invite her} male {Invite him to {{receiver.name}} party} other {Maybe not}
recipients[0].gender, select, male {You ({{sender.name}}) gave } female {You gave her} other {You gave them}
recipients[0].gender, select, !male {You ({{sender.name}}) gave } ?female {You gave her} other {You gave them}